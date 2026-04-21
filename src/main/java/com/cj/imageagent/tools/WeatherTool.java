package com.cj.imageagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WeatherTool {
    @Tool(description = "查询指定城市和日期的天气，参数：城市、日期(yyyy-MM-dd)")
    public String getWeather(
            @ToolParam(description = "需要查询天气的城市名称，如：南昌、北京") String city,
            @ToolParam(description = "要查询的日期，格式 yyyy-MM-dd，不传默认今天") String date
    ) {
        // 这里可以调用真实天气接口
        log.info("WeatherTool调用: 查询城市 {} 日期 {} 的天气", city, date.isEmpty() ? "今天" : date);
        return city + " " + (date.isEmpty() ? "今天" : date) + "的天气：晴，温度 22~28℃，微风";
    }
}
