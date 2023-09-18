package com.example.demo.helper;

import lombok.Data;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;

import java.util.Map;

/**
 * 映射表达式计算 Helper 类
 * @author jianjie.lty
 * @date 2023/8/14
 */
public class ExpressionParseHelper {
    public Object calculate(String expressionStr, MeterMapperContext context) {
        // 使用表达式引擎计算表达式结果
        JexlEngine engine = new JexlBuilder().create();
        JexlExpression expression = engine.createExpression(expressionStr);
        Object result = expression.evaluate(context);
        return result;
    }

    @Data
    public static class MeterMapperContext implements JexlContext {
        private Map<String, Object> SplitItemBill;
        // 实现get方法来获取变量的值
        @Override
        public Object get(String name) {
            if (SplitItemBill.containsKey(name)) {
                // 从账单内获取变量的值
                return SplitItemBill.get(name);
            } else {
                // 用户输入的变量，在账单内并不能找到同名变量
                throw new RuntimeException("No such BillItem as: " + name);
            }
        }

        // 实现set方法来设置变量的值
        @Override
        public void set(String name, Object value) {
        }

        @Override
        public boolean has(String s) {
            return false;
        }
    }
}

