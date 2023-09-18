package com.example.demo.helper;

import com.example.demo.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Json文件解析 Helper 类
 * @author jianjie.lty
 * @date 2023/8/14
 */
public class JsonParseHelper {
    public <T> T parseJsonMetadata(String jsonMetadataString, Class<T> clazz) {
        if (StringUtils.isBlank(jsonMetadataString)) {
            throw new RuntimeException("Empty json string can not be parsed.");
        }
        T model;
        try {
            model = JsonUtil.parseObjectUpperCamelCase(jsonMetadataString, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return model;
    }
}
