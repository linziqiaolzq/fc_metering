package com.example.demo.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 账单数据模型类
 * @author jianjie.lty
 * @date 2023/8/16
 */
@Data
public class BillModel {
    @SerializedName("Data")
    private BillData billData;

    @Data
    public class BillData {
        private String billingCycle;

        private int totalCount;

        private Long accountID;

        private int maxResults;

        @SerializedName("Items")
        private List<Map<String, Object>> items;
    }
}
