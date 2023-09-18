package com.example.demo.model;

import com.example.demo.helper.MeterRequestHelper;
import com.example.demo.util.JsonUtil;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计量数据模型类
 * @author jianjie.lty
 * @date 2023/8/17
 */
@Data
public class MeteringMetadata implements Serializable {
    private Map<String, Integer> keyValueMap;

    private String startTime;

    private String endTime;

    private List<Entity> entities;

    private static final String START_TIME = "StartTime";

    private static final String END_TIME = "EndTime";

    private static final String ENTITIES = "Entities";

    public class Entity implements Serializable {
        private String key;
        private String value;

        public Entity(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public MeteringMetadata() {
        keyValueMap = new HashMap<>();
    }

    public void addOrUpdate(String key, Integer value) {
        if (keyValueMap.containsKey(key)) {
            Integer newValue = value + keyValueMap.get(key);
            keyValueMap.put(key, newValue);
        } else {
            keyValueMap.put(key, value);
        }
    }

    public Integer getValue(String key) {
        return keyValueMap.get(key);
    }

    public String buildMeteringInput() {
        this.entities = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : keyValueMap.entrySet()) {
            entities.add(new Entity(entry.getKey(), entry.getValue().toString()));
        }
        if (CollectionUtils.isEmpty(entities)) {
            return null;
        }
        MeterRequestHelper meterRequestHelper = new MeterRequestHelper();
        MeterRequestHelper.GetMeterTimeResult getMeterTimeResult = meterRequestHelper.getMeterTimeStamp();
        this.startTime = getMeterTimeResult.getStartTime().toString();
        this.endTime = getMeterTimeResult.getEndTime().toString();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(START_TIME, startTime);
        resultMap.put(END_TIME, endTime);
        resultMap.put(ENTITIES, entities);

        List<Map> resultList = new ArrayList<>();
        resultList.add(resultMap);
        String resultJson = JsonUtil.toJsonString(resultList);
        return resultJson;
    }
}


