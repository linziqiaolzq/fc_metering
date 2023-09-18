package com.example.demo.service.impl;

import com.aliyun.bssopenapi20171214.models.DescribeSplitItemBillResponse;
import com.aliyun.bssopenapi20171214.models.DescribeSplitItemBillResponseBody;
import com.aliyun.computenestsupplier20210521.models.GetServiceInstanceResponse;
import com.aliyun.computenestsupplier20210521.models.GetServiceResponse;
import com.aliyun.computenestsupplier20210521.models.GetServiceResponseBody;
import com.aliyun.computenestsupplier20210521.models.ListServiceInstancesResponse;
import com.aliyun.computenestsupplier20210521.models.ListServiceInstancesResponseBody;
import com.aliyun.computenestsupplier20210521.models.PushMeteringDataResponse;
import com.example.demo.adapter.BssClient;
import com.example.demo.adapter.ComputeNestClient;
import com.example.demo.aliyun.ExecuteContext;
import com.example.demo.helper.ExpressionParseHelper;
import com.example.demo.model.InstanceConfigInnerKeys;
import com.example.demo.model.MeterDataSource;
import com.example.demo.model.MeterMapperModel;
import com.example.demo.model.MeteringMetadata;
import com.example.demo.service.AutoMeteringService;
import com.example.demo.util.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jianjie.lty
 * @date 2023/8/14
 */
@Service
public class AutoMeteringServiceImpl implements AutoMeteringService {
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String MONTH_FORMAT = "yyyy-MM";

    private static final String GRANULARITY = "DAILY";

    private static final String INSTANCE_CONFIG = "InstanceConfig";

    private static final String BILLING_ITEM_CODE = "BillingItemCode";

    private static final String CUSTOM = "Custom";

    private static final String INSTANCE_TYPE = "InstanceType";

    private static final String PUSH_METERING_DATA_SOURCE = "PushMeteringDataSource";

    private static final String ITEMS = "Items";

    private static final String MANAGED = "managed";

    private static final String PAY_AS_YOU_GO = "PayAsYouGo";

    private static final String MARKET = "Market";

    @Value("${file.path}")
    private String file_path;

    @Autowired
    private ComputeNestClient computeNestClient;

    @Autowired
    private BssClient bssClient;

    @Override
    public Map<String, Integer> autoMetering(ExecuteContext executeContext, String serviceId, String serviceVersion, String billingDate) throws Exception {
        Integer successCount = 0;
        Integer totalCount = 0;
        // 若服务非全托管或无云市场商品链接，抛异常
        validateService(executeContext, serviceId);
        Map<String, String> serviceInstanceInfos = getServiceInstanceInfos(executeContext, serviceId);
        System.out.printf("Get serviceInstanceInfos success: %s%n", serviceInstanceInfos);
        // 遍历每个服务实例，根据模板和套餐类型，判断出需要本FC上报的计量项（在服务实例上报项中且上报方式为服务商上报），
        for (String serviceInstanceId : serviceInstanceInfos.keySet()) {
            String serviceInstanceVersion = serviceInstanceInfos.get(serviceInstanceId);
            if (StringUtils.isNotBlank(serviceVersion) && !serviceInstanceVersion.equals(serviceVersion)) {
                // 若服务商输入serviceVersion，则跳过所有非此版本的服务实例
                continue;
            }
            System.out.printf("---------------------------------- ServiceInstance %s AutoMetering Start ----------------------------------%n", serviceInstanceId);
            GetServiceInstanceResponse getServiceInstanceResponse = computeNestClient.getServiceInstance(executeContext, serviceInstanceId);
            System.out.printf("Get serviceInstance success: %s, requestId: %s%n", serviceInstanceId, getServiceInstanceResponse.getBody().getRequestId());
            if (!validateServiceInstance(getServiceInstanceResponse)) {
                // 若服务实例非云市场或非按量付费，跳过
                continue;
            }
            String predefinedParameterName = getServiceInstanceResponse.getBody().getPredefinedParameterName();
            String templateName = getServiceInstanceResponse.getBody().getTemplateName();
            String serviceInstanceMeterType = predefinedParameterName + templateName;
            // 根据服务id，服务实例计量类型（[模板，套餐]二元组），服务实例版本，确定本服务实例的有效上报项effectiveMeterItems
            Set<String> effectiveMeterItems = buildEffectiveMeterItemSet(executeContext, serviceId, serviceInstanceMeterType, serviceInstanceVersion);
            System.out.printf("Effective meter items: %s%n", effectiveMeterItems);

            // 获取当前日期
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            SimpleDateFormat monthFormat = new SimpleDateFormat(MONTH_FORMAT);

            // 利用账单能力，获取服务实例账单数据
            String granularity = GRANULARITY;
            Date date = dateFormat.parse(billingDate);
            String billingCycle = monthFormat.format(date);
            DescribeSplitItemBillResponse response = bssClient.describeSplitItemBill(executeContext, serviceInstanceId, billingCycle, billingDate, granularity);
            DescribeSplitItemBillResponseBody.DescribeSplitItemBillResponseBodyData billData = response.getBody().getData();
            System.out.printf("Describe split item bill success, requestId: %s%n", response.getBody().getRequestId());
            Map billDataMap = billData.toMap();
            List<HashMap> billItems = (List<HashMap>) billDataMap.get(ITEMS);

            // 读取映射规则文件，按照映射规则将账单数据映射为计量项数据
            String jsonString = readFileFromResources(file_path);
            MeterMapperModel meterMapperModel = MeterMapperModel.parseMappingMetaData(jsonString);
            Map<String, List<MeterMapperModel.MeterMapperItem>> meterMappers = meterMapperModel.getMeterMappers();

            // 处理账单中每一项用量数据billItem，映射为计量数据
            MeteringMetadata meteringMetadata = new MeteringMetadata();
            for (HashMap billItem : billItems) {
                String billingItemCode = billItem.get(BILLING_ITEM_CODE).toString();
                String instanceConfigMetaData = billItem.get(INSTANCE_CONFIG).toString();
                List<MeterMapperModel.MeterMapperItem> listMeterMapperItem = meterMappers.get(billingItemCode);
                if (CollectionUtils.isNotEmpty(listMeterMapperItem)) {
                    for (MeterMapperModel.MeterMapperItem meterMapperItem : listMeterMapperItem) {
                        ExpressionParseHelper expressionParseHelper = new ExpressionParseHelper();
                        ExpressionParseHelper.MeterMapperContext mapperContext = new ExpressionParseHelper.MeterMapperContext();
                        String meterItem = meterMapperItem.getKey();
                        String expression = meterMapperItem.getValue();
                        if (!effectiveMeterItems.contains(meterItem)) {
                            // 只处理有效的映射项
                            continue;
                        }
                        if (INSTANCE_TYPE.equals(billingItemCode)) {
                            // 账单项InstanceType需要特殊处理(可能要进一步解析表达式中instanceConfig内部键值对，更新映射公式的数据源billItem)
                            HashMap newBillItem = updateBillItemByInstanceConfig(expression, instanceConfigMetaData, billItem);
                            if (MapUtils.isEmpty(newBillItem)) {
                                continue;
                            }
                            mapperContext.setSplitItemBill(newBillItem);
                        } else {
                            mapperContext.setSplitItemBill(billItem);
                        }
                        // 利用数据源billItem，计算表达式expression的映射值，更新上报元数据meteringMetadata
                        Object meterValue = expressionParseHelper.calculate(expression, mapperContext);
                        Integer integerMeterValue = Integer.valueOf(String.valueOf(meterValue));
                        System.out.printf("MeterItem: %s, expression: %s, computation result: %s%n", meterItem, expression, integerMeterValue);
                        meteringMetadata.addOrUpdate(meterItem, integerMeterValue);
                    }
                } else {
                    // 映射文件中，不存在这个账单项的转换公式（也许意味着服务商不想统计，因此可以不抛异常）
                    System.out.printf("Mapping expression not defined for %s in %s%n", billingItemCode, file_path);
                }
            }

            // 拼凑pushMeteringData格式并上报
            String metering = meteringMetadata.buildMeteringInput();
            if (StringUtils.isNotBlank(metering)) {
                System.out.printf("Metering data for %s : %s%n", serviceInstanceId, metering);
                try {
                    PushMeteringDataResponse pushMeteringDataResponse = computeNestClient.pushMeteringData(executeContext,
                            metering, serviceInstanceId);
                    System.out.printf("Push metering data response for %s: %s%n", serviceInstanceId, JsonUtil.toJsonString(pushMeteringDataResponse));
                    successCount += 1;
                } catch (Exception e) {
                    System.out.printf("Push metering data failed for %s%n", serviceInstanceId);
                }
            } else {
                System.out.printf("No metering data for serviceInstance %s%n", serviceInstanceId);
            }
            System.out.printf("---------------------------------- ServiceInstance %s AutoMetering End ----------------------------------%n%n", serviceInstanceId);
            totalCount += 1;
        }
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("totalCount", totalCount);
        resultMap.put("successCount", successCount);
        return resultMap;
    }

    private HashMap updateBillItemByInstanceConfig(String expression, String instanceConfigMetaData, HashMap billItem) {
        // 账单项InstanceType需要特殊处理(可能要进一步解析表达式中instanceConfig内部键值对，更新映射公式的数据源billItem)
        Map<String, String> instanceConfigMetaMap = parseInstanceConfigMapFromMetaData(instanceConfigMetaData);
        List<String> innerKeys = parseInstanceConfigInnerKeysFromExpression(expression);
        if (CollectionUtils.isNotEmpty(innerKeys)) {
            if (MapUtils.isNotEmpty(instanceConfigMetaMap)) {
                // 表达式包含InstanceConfig内变量，且账单项有InstanceConfig数据
                Map<String, Object> instanceConfigExpressionMap = new HashMap<>();
                for (String innerKey : innerKeys) {
                    Integer innerValue = parseInnerValueFromInstanceConfigMap(instanceConfigMetaMap, innerKey);
                    instanceConfigExpressionMap.put(innerKey, innerValue);
                }
                billItem.put(INSTANCE_CONFIG, instanceConfigExpressionMap);
            } else {
                // 不同时满足条件，则跳过本次计算
                return null;
            }
        }
        return billItem;
    }

    private Boolean validateServiceInstance(GetServiceInstanceResponse getServiceInstanceResponse) {
        String source = getServiceInstanceResponse.getBody().getSource();
        String payType = getServiceInstanceResponse.getBody().getPayType();
        String serviceInstanceId = getServiceInstanceResponse.getBody().getServiceInstanceId();
        if (!MARKET.equals(source)) {
            // 服务实例非云市场部署，跳过
            System.out.printf("Only market-sourced serviceInstance should be metered, %s is %s%n", serviceInstanceId, source);
            return false;
        }
        if (!PAY_AS_YOU_GO.equals(payType)) {
            // 服务实例非云市场按量付费，跳过
            System.out.printf("Only PayAsYouGo serviceInstance should be metered, %s is %s%n", serviceInstanceId, payType);
            return false;
        }
        return true;
    }

    private void validateService(ExecuteContext executeContext, String serviceId) throws Exception {
        GetServiceResponse getServiceResponse = computeNestClient.getService(executeContext, serviceId);
        String serviceType = getServiceResponse.getBody().getServiceType();
        String commodityCode = getServiceResponse.getBody().getCommodityCode();
        if (!MANAGED.equals(serviceType)) {
            System.out.printf("Only managed service should be metered, %s is %s%n", serviceId, serviceType);
            // 若服务非全托管服务，抛异常
            throw new RuntimeException("Service type should be managed.");
        }
        if (commodityCode == null) {
            System.out.printf("Only service with commodityCode should be metered, %s commodityCode is null%n", serviceId);
            // 若服务无云市场商品链接，抛异常
            throw new RuntimeException("Service should have commodityCode.");
        }
    }
    private List<String> parseInstanceConfigInnerKeysFromExpression(String expression) {
        List<String> innerKeys = new ArrayList<>();
        // 定义正则表达式，并使用捕获组提取变量名称
        String regex = "InstanceConfig\\.(\\w+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(expression);

        // 提取匹配的变量名称
        while (matcher.find()) {
            String innerKey = matcher.group(1); // 获取捕获组中的内容
            innerKeys.add(innerKey);
        }
        return innerKeys;
    }
    private Set<String> buildEffectiveMeterItemSet(ExecuteContext executeContext, String serviceId, String serviceInstanceMeterType, String serviceInstanceVersion) throws Exception {
        GetServiceResponse getServiceResponse = computeNestClient.getService(executeContext, serviceId, serviceInstanceVersion);
        Set<String> effectiveMeterItems = new HashSet<>();
        System.out.printf("Get service success: %s, requestId: %s%n", serviceId, getServiceResponse.getBody().getRequestId());
        String deployMetadata = getServiceResponse.getBody().getDeployMetadata();
        Map deployMetadataMap = JsonUtil.parseObjectUpperCamelCase(deployMetadata, Map.class);
        // 查询该服务的上报设置，共有哪些计量项，每个计量项的上报方式
        Map<String, String> pushMeteringDataSource = (Map) deployMetadataMap.get(PUSH_METERING_DATA_SOURCE);
        // 查询该服务的计量项组合列表，每个元素为一个（套餐，模版）二元组确定的计量项组合
        Map<String, Set<String>> commodityEntities = getCommodityEntitiesFromGetServiceResponse(getServiceResponse);
        Set meterEntities = commodityEntities.get(serviceInstanceMeterType);

        // 输入服务实例的计量项和所属服务的上报计量项，取交集得到实际需要本FC上报的计量项
        MeterDataSource[] meterDataSources = MeterDataSource.values();
        for (MeterDataSource meterDataSource : meterDataSources) {
            if (CUSTOM.equals(pushMeteringDataSource.get(meterDataSource.getValue())) && meterEntities.contains(meterDataSource.getValue())) {
                effectiveMeterItems.add(meterDataSource.getValue());
            }
        }
        return effectiveMeterItems;
    }

    private Map<String, String> getServiceInstanceInfos(ExecuteContext executeContext, String serviceId) throws Exception {
        // 查询该服务的全部服务实例id列表
        Map<String, String> serviceInstanceInfos = new HashMap<>();
        ListServiceInstancesResponse listServiceInstancesResponse;
        String nextToken = "";
        int maxResult = 100;
        while (true) {
            listServiceInstancesResponse = computeNestClient.listServiceInstances(executeContext, serviceId, nextToken, maxResult);
            List<ListServiceInstancesResponseBody.ListServiceInstancesResponseBodyServiceInstances> serviceInstances =
                    listServiceInstancesResponse.getBody().getServiceInstances();
            System.out.printf("List service instances success, requestId: %s%n", listServiceInstancesResponse.getBody().getRequestId());
            // 提取当前分页中的全部服务示例id
            for (ListServiceInstancesResponseBody.ListServiceInstancesResponseBodyServiceInstances obj : serviceInstances) {
                String serviceInstanceId = obj.getServiceInstanceId();
                String serviceVersion = obj.getService().getVersion();
                serviceInstanceInfos.put(serviceInstanceId, serviceVersion);
            }
            // 根据nextToken信息，遍历下一页的服务实例
            nextToken = listServiceInstancesResponse.getBody().getNextToken();
            if (StringUtils.isEmpty(nextToken)) {
                break;
            }
        }
        return serviceInstanceInfos;
    }

    private Map<String, Set<String>> getCommodityEntitiesFromGetServiceResponse(GetServiceResponse getServiceResponse) {
        List<GetServiceResponseBody.GetServiceResponseBodyCommodityEntities> commodityEntitiesMetaData = getServiceResponse.getBody().getCommodityEntities();
        Map<String, Set<String>> commodityEntities = new HashMap<>();
        for (GetServiceResponseBody.GetServiceResponseBodyCommodityEntities commodityEntity : commodityEntitiesMetaData) {
            Set<String> entities = new HashSet<>();
            for (String entity : commodityEntity.getEntityIds()) {
                // 根据返回格式解析该服务的映射项
                int firstDashIndex = entity.indexOf('-');
                int secondDashIndex = entity.indexOf('-', firstDashIndex + 1);
                if (firstDashIndex != -1 && secondDashIndex != -1) {
                    String extractedString = entity.substring(firstDashIndex + 1, secondDashIndex);
                    entities.add(extractedString);
                }
            }
            String commodityKey = commodityEntity.predefinedParameterName + commodityEntity.templateName;
            commodityEntities.put(commodityKey, entities);
        }
        return commodityEntities;
    }

    private Map<String, String> parseInstanceConfigMapFromMetaData(String instanceConfigMetaData) {
        Map<String, String> instanceConfigMetaMap = new HashMap<>();
        String[] pairs = instanceConfigMetaData.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String keyMap = keyValue[0].trim();
                String valueMap = keyValue[1].trim();
                instanceConfigMetaMap.put(keyMap, valueMap);
            }
        }
        return instanceConfigMetaMap;
    }
    private Integer parseInnerValueFromInstanceConfigMap(Map<String, String> instanceConfigMap, String innerExpressionKey) {
        // 解析item中的InstanceConfig的内层key的有效值
        Integer innerValue = 0;
        String innerMetaKey = InstanceConfigInnerKeys.get(innerExpressionKey);
        String innerValueMetaString = instanceConfigMap.get(innerMetaKey);
        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(innerValueMetaString);
        if (matcher.find()) {
            String innerValueString = matcher.group();
            innerValue = Integer.parseInt(innerValueString);
        } else {
            System.out.printf("Inner value for %s not found in InstanceConfig %n", innerExpressionKey);
        }
        return innerValue;
    }

    public String readFileFromResources(String path) {
        // 从resource目录下读取映射规则
        ClassPathResource classPathResource = new ClassPathResource(path);
        BufferedReader bufferedReader = null;
        String content = "";
        try {
            InputStream inputStream = classPathResource.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);

            String temp = null;
            while ((temp = bufferedReader.readLine()) != null) {
                content += temp + "\n";
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }
}