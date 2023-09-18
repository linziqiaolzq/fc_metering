package com.example.demo.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 账单InstanceConfig内部特殊键枚举类
 * @author jianjie.lty
 * @date 2023/9/6
 */
@Getter
public enum InstanceConfigInnerKeys {
    CPU("CPU", "CPU"),

    RAM("RAM", "内存"),

    DISK("DISK", "磁盘");

    private String metaValue;

    private String expressionValue;

    private static final Map<String, String> KEY_MAP = new HashMap<>();

    static {
        for (InstanceConfigInnerKeys key : InstanceConfigInnerKeys.values()) {
            KEY_MAP.put(key.metaValue, key.expressionValue);
        }
    }

    InstanceConfigInnerKeys(String metaValue, String expressionValue) {
        this.metaValue = metaValue;
        this.expressionValue = expressionValue;
    }

    /**
     * 根据键值获取对应的枚举常量
     *
     * @param key
     * @return 对应的枚举常量，如果找不到匹配的常量则返回null
     */
    public static String get(String key) {
        return KEY_MAP.get(key);
    }
}
