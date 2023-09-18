package com.example.demo.adapter;
import com.aliyun.computenestsupplier20210521.models.GetServiceInstanceResponse;
import com.aliyun.computenestsupplier20210521.models.GetServiceResponse;
import com.aliyun.computenestsupplier20210521.models.ListServiceInstancesResponse;
import com.aliyun.computenestsupplier20210521.models.PushMeteringDataResponse;
import com.example.demo.aliyun.ExecuteContext;

/**
 * @author jianjie.lty
 * @date 2023/8/14
 */
public interface ComputeNestClient {

    /**
     * 获取计算巢服务
     */
    GetServiceResponse getService(ExecuteContext executeContext, String serviceId, String serviceVersion) throws Exception;

    GetServiceResponse getService(ExecuteContext executeContext, String serviceId) throws Exception;

    /**
     * 上报计算巢服务实例的计量数据至云市场
     */
    PushMeteringDataResponse pushMeteringData(ExecuteContext executeContext, String metering, String serviceInstanceId) throws Exception;

    /**
     * 分页获取计算巢服务实例列表
     */
    ListServiceInstancesResponse listServiceInstances(ExecuteContext executeContext, String serviceId, String nextToken, int maxResult) throws Exception;

    /**
     * 获取计算巢服务实例
     */
    GetServiceInstanceResponse getServiceInstance(ExecuteContext executeContext, String serviceInstanceId) throws Exception;
}
