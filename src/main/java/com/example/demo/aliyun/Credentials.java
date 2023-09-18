package com.example.demo.aliyun;

import lombok.Data;

/**
 * @author jianjie.lty
 * @date 2023/9/4
 */
@Data
public class Credentials {
    private String accessKeyId;

    private String accessKeySecret;

    private String securityToken;

    public Credentials(String accessKeyId, String accessKeySecret, String securityToken) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.securityToken = securityToken;
    }
}
