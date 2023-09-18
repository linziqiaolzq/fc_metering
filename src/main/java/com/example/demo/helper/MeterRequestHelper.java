package com.example.demo.helper;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 上报时间戳生成 Helper 类
 * @author jianjie.lty
 * @date 2023/8/14
 */
public class MeterRequestHelper {

    @Data
    public class GetMeterTimeResult {
        private Long startTime;

        private Long endTime;
    }
    public GetMeterTimeResult getMeterTimeStamp() {
        // 构造上报时间戳
        long currentTimeStamp = System.currentTimeMillis() / 1000;
        GetMeterTimeResult getMeterTimeResult = new GetMeterTimeResult();
        getMeterTimeResult.setStartTime(currentTimeStamp - 1);
        getMeterTimeResult.setEndTime(currentTimeStamp);
        return getMeterTimeResult;
    }
}
