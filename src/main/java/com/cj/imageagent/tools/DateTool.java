package com.cj.imageagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiFunction;

@Slf4j
@Component
public class DateTool  {
    @Tool(description = "获取当前时间，格式为 yyyy-MM-dd")
    public String getLocalDate() {
        log.info("DateTool调用: 获取当前时间");
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}

//public class DateTool implements BiFunction<DateTool.DateRequest, ToolContext, String> {
//
//    // 1. 定义一个简单的参数包装类（解决 inputType 不能为 null 的核心）
//    public record DateRequest(String message) {}
//
//    @Override
//    public String apply(DateRequest request, ToolContext context) {
//        log.info("DateTool调用: 收到消息 {}, 获取当前时间", request.message());
//        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//    }
//
//    @Bean
//    public ToolCallback dateToolCallback(){
//        return FunctionToolCallback.builder("getCurrentDate", this)
//                .description("获取当前日期时间，当用户询问今天、日期或时间相关问题时使用")
//                .inputType(DateRequest.class) // 显式注入类型，修复报错
//                .build();
//    }
//}