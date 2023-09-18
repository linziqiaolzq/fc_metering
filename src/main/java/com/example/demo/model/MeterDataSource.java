package com.example.demo.model;

import lombok.Getter;


/**
 * 计量项枚举类
 * @author jianjie.lty
 * @date 2023/8/16
 */
@Getter
public enum MeterDataSource {
    // 使用存储空间（Byte）
    STORAGE("Storage"),

    // 下行使用流量（Bit）
    NETWORK_IN("NetworkIn"),

    // 上行使用流量（Bit）
    NETWORK_OUT("NetworkOut"),

    // Period（使用时长）
    PERIOD("Period"),

    // 频率
    FREQUENCY("Frequency"),

    // 使用分钟时长（分钟）
    PERIOD_MIN("PeriodMin"),

    // 虚拟CPU核数
    VIRTUAL_CPU("VirtualCpu"),

    // Memory(GB)
    MEMORY("Memory"),

    // Unit(个)
    Unit("Unit"),

    // 日活跃用户数（DAU）
    DAILY_ACTIVE_USER("DailyActiveUser"),

    // 字符数（个）
    CHARACTER("Character");

    private String value;

    MeterDataSource(String value) {
        this.value = value;
    }
}
