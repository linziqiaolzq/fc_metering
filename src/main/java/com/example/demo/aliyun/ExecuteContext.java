package com.example.demo.aliyun;

import lombok.Data;

/**
 * @author jianjie.lty
 * @date 2023/9/4
 */
@Data
public class ExecuteContext {
    private Credentials credentials;

    private String regionId;
}
