package com.example.demo.adapter;
import com.aliyun.bssopenapi20171214.models.DescribeSplitItemBillResponse;
import com.example.demo.aliyun.ExecuteContext;

/**
 * @author jianjie.lty
 * @date 2023/8/14
 */
public interface BssClient {

    /**
     * 按天获取计算巢服务实例的分账账单
     */
    DescribeSplitItemBillResponse describeSplitItemBill(ExecuteContext executeContext, String serviceInstanceId,
                                        String billingCycle, String billingDate, String granularity) throws Exception;

}
