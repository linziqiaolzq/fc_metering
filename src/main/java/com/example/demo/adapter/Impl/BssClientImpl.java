package com.example.demo.adapter.Impl;

import com.aliyun.bssopenapi20171214.Client;
import com.aliyun.bssopenapi20171214.models.DescribeSplitItemBillRequest;
import com.aliyun.bssopenapi20171214.models.DescribeSplitItemBillResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.example.demo.adapter.BssClient;
import com.example.demo.aliyun.ExecuteContext;
import org.springframework.stereotype.Component;

/**
 * @author jianjie.lty
 * @date 2023/8/14
 */
@Component
public class BssClientImpl implements BssClient {
    private static final String COMPUTENEST_TAG_KEY = "acs:computenest:serviceInstanceId";

    private com.aliyun.bssopenapi20171214.Client createClient(ExecuteContext executeContext) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.accessKeyId = executeContext.getCredentials().getAccessKeyId();
        config.accessKeySecret = executeContext.getCredentials().getAccessKeySecret();
        config.securityToken = executeContext.getCredentials().getSecurityToken();
        config.regionId = executeContext.getRegionId();
        return new com.aliyun.bssopenapi20171214.Client(config);
    }

    @Override
    public DescribeSplitItemBillResponse describeSplitItemBill(ExecuteContext executeContext, String serviceInstanceId, String billingCycle, String billingDate, String granularity) throws Exception {
        Client client = createClient(executeContext);
        DescribeSplitItemBillRequest.DescribeSplitItemBillRequestTagFilter tagFilter = new DescribeSplitItemBillRequest.DescribeSplitItemBillRequestTagFilter()
                .setTagKey(COMPUTENEST_TAG_KEY)
                .setTagValues(java.util.Arrays.asList(
                        serviceInstanceId
                ));
        DescribeSplitItemBillRequest describeSplitItemBillRequest = new DescribeSplitItemBillRequest()
                .setBillingCycle(billingCycle)
                .setGranularity(granularity)
                .setBillingDate(billingDate)
                .setTagFilter(java.util.Arrays.asList(
                        tagFilter
                ));
        RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        DescribeSplitItemBillResponse response = client.describeSplitItemBillWithOptions(describeSplitItemBillRequest, runtime);
        return response;
    }
}
