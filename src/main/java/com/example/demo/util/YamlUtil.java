package com.example.demo.util;

import com.alibaba.cachejson.JSONObject;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.Map;

/**
 * @author zhaoyu.zhaoyu
 * @date 2021/07/20
 */
public class YamlUtil {

    public static String convertToJsonString(String yamlString) {
        Yaml yaml = new Yaml(new SafeConstructor());
        Map<String, Object> map = yaml.load(yamlString);

        JSONObject jsonObject = new JSONObject(map);
        return jsonObject.toString();
    }
}
