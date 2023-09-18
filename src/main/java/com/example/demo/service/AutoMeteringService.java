package com.example.demo.service;

import com.example.demo.aliyun.ExecuteContext;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author jianjie.lty
 * @date 2023/8/14
 */
public interface AutoMeteringService {
    public Map<String, Integer> autoMetering(ExecuteContext executeContext, String serviceId, String serviceVersion, String billingDate) throws Exception;
}
