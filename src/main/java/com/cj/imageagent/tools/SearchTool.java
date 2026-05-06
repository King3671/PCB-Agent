package com.cj.imageagent.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

// 定义工具（示例：仅一个搜索工具）
@Component
public class SearchTool {
    @Tool( description = "网页搜索工具，当需要搜索信息时使用，参数：查询内容")
    public String getSearch(@ToolParam(description = "需要搜索的内容") String query) {
        // 假设这里调用了一个搜索API，并返回了结果
        return query;
    }
}
//public class SearchTool implements BiFunction<String, ToolContext, String> {
//    @Override
//    public String apply(String query, ToolContext context) {
//        // 实现搜索逻辑
//        return "搜索结果: " + query;
//    }
//
//    @Bean
//    public ToolCallback searchToolCallback(){
//        return FunctionToolCallback.builder("search", this)
//                .description("搜索工具")
//                .build();
//    }
//}
