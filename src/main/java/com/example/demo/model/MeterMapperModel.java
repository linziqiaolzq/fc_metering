package com.example.demo.model;

import com.example.demo.helper.JsonParseHelper;
import com.example.demo.util.YamlUtil;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import java.io.IOException;
import java.util.Map;
import java.util.List;

/**
 * 映射规则模型类
 * @author jianjie.lty
 * @date 2023/8/14
 */
@Data
public class MeterMapperModel {
    @SerializedName("MeterMappers")
    private Map<String, List<MeterMapperItem>> meterMappers;

    @Data
    public class MeterMapperItem {
        @SerializedName("Key")
        private String key;

        @SerializedName("Value")
        private String value;
    }

    private static boolean isJsonFormat(String content) {
        if (content.trim().startsWith("{")) {
            return true;
        } else {
            return false;
        }
    }

    public static MeterMapperModel parseMappingMetaData(String jsonMetadataString) throws IOException {
        // 解析json或yaml文件，初始化计量项映射模型
        MeterMapperModel meterMapperModel;
        if (!isJsonFormat(jsonMetadataString)) {
            jsonMetadataString = YamlUtil.convertToJsonString(jsonMetadataString);
        }
        meterMapperModel = new JsonParseHelper().parseJsonMetadata(jsonMetadataString, MeterMapperModel.class);
        return meterMapperModel;
    }
}
