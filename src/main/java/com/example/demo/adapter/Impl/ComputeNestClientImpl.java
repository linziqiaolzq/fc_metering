package com.example.demo.adapter.Impl;

import com.aliyun.computenestsupplier20210521.models.GetServiceInstanceRequest;
import com.aliyun.computenestsupplier20210521.models.GetServiceInstanceResponse;
import com.aliyun.computenestsupplier20210521.models.GetServiceResponse;
import com.aliyun.computenestsupplier20210521.models.ListServiceInstancesRequest;
import com.aliyun.computenestsupplier20210521.models.ListServiceInstancesResponse;
import com.aliyun.computenestsupplier20210521.models.PushMeteringDataResponse;
import com.aliyun.computenestsupplier20210521.models.PushMeteringDataRequest;
import com.aliyun.computenestsupplier20210521.models.GetServiceRequest;
import com.aliyun.computenestsupplier20210521.Client;
import com.example.demo.adapter.ComputeNestClient;
import com.aliyun.teautil.models.RuntimeOptions;
import com.example.demo.aliyun.ExecuteContext;
import org.springframework.stereotype.Component;

/**
 * @author jianjie.lty
 * @date 2023/8/14
 */
@Component
public class ComputeNestClientImpl implements ComputeNestClient {
    private static final String SERVICE_ID = "ServiceId";

    private Client createClient(ExecuteContext executeContext) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.accessKeyId = executeContext.getCredentials().getAccessKeyId();
        config.accessKeySecret = executeContext.getCredentials().getAccessKeySecret();
        config.securityToken = executeContext.getCredentials().getSecurityToken();
        config.regionId = executeContext.getRegionId();
        return new Client(config);
    }

    @Override
    public GetServiceResponse getService(ExecuteContext executeContext, String serviceId, String serviceVersion) throws Exception {
        Client client = createClient(executeContext);
        GetServiceRequest getServiceRequest = new GetServiceRequest()
                .setRegionId(executeContext.getRegionId())
                .setServiceId(serviceId)
                .setServiceVersion(serviceVersion);
        RuntimeOptions runtime = new RuntimeOptions();
        GetServiceResponse getServiceResponse = client.getServiceWithOptions(getServiceRequest, runtime);
        return getServiceResponse;
    }

    @Override
    public GetServiceResponse getService(ExecuteContext executeContext, String serviceId) throws Exception {
        Client client = createClient(executeContext);
        GetServiceRequest getServiceRequest = new GetServiceRequest()
                .setRegionId(executeContext.getRegionId())
                .setServiceId(serviceId);
        RuntimeOptions runtime = new RuntimeOptions();
        GetServiceResponse getServiceResponse = client.getServiceWithOptions(getServiceRequest, runtime);
        return getServiceResponse;
    }

    @Override
    public PushMeteringDataResponse pushMeteringData(ExecuteContext executeContext, String metering, String serviceInstanceId) throws Exception {
        Client client = createClient(executeContext);
        PushMeteringDataRequest pushMeteringDataRequest = new PushMeteringDataRequest()
            .setMetering(metering)
            .setServiceInstanceId(serviceInstanceId);
        RuntimeOptions runtime = new RuntimeOptions();
        PushMeteringDataResponse pushMeteringDataResponse = client.pushMeteringDataWithOptions(pushMeteringDataRequest, runtime);
        return pushMeteringDataResponse;
    }

    @Override
    public ListServiceInstancesResponse listServiceInstances(ExecuteContext executeContext, String serviceId, String nextToken, int maxResult) throws Exception {
        Client client = createClient(executeContext);
        ListServiceInstancesRequest.ListServiceInstancesRequestFilter listServiceInstancesRequestFilter = new ListServiceInstancesRequest
                .ListServiceInstancesRequestFilter()
                .setName(SERVICE_ID)
                .setValue(java.util.Arrays.asList(
                        serviceId
                ));
        ListServiceInstancesRequest listServiceInstancesRequest = new ListServiceInstancesRequest()
                .setRegionId(executeContext.getRegionId())
                .setFilter(java.util.Arrays.asList(
                        listServiceInstancesRequestFilter
                ))
                .setMaxResults(maxResult)
                .setNextToken(nextToken);
        RuntimeOptions runtime = new RuntimeOptions();
        ListServiceInstancesResponse listServiceInstancesResponse = client.listServiceInstancesWithOptions(listServiceInstancesRequest, runtime);
        return listServiceInstancesResponse;
    }

    @Override
    public GetServiceInstanceResponse getServiceInstance(ExecuteContext executeContext, String serviceInstanceId) throws Exception {
        Client client = createClient(executeContext);
        GetServiceInstanceRequest getServiceInstanceRequest = new GetServiceInstanceRequest()
                .setRegionId(executeContext.getRegionId())
                .setServiceInstanceId(serviceInstanceId);
        RuntimeOptions runtime = new RuntimeOptions();
        GetServiceInstanceResponse getServiceInstanceResponse = client.getServiceInstanceWithOptions(getServiceInstanceRequest, runtime);
        return getServiceInstanceResponse;
    }
}
