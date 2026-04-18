package com.cj.imageagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 天气查询工具
@Slf4j
@Component
public class WeatherForLocationTool {
    /**
     * 工具1：获取当前时间（无参）
     * @Tool 必须写 description，供大模型判断是否调用
     */
    @Tool(description = "获取当前系统日期和时间，格式为 yyyy-MM-dd HH:mm:ss")
    public String getCurrentTime() {
        log.info("【工具被调用】getCurrentTime 执行");
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 工具2：计算两数之和（带参）
     * @ToolParam 描述参数，引导模型正确传参
     */
    @Tool(description = "计算两个整数的和")
    public int add(
            @ToolParam(description = "第一个整数") int a,
            @ToolParam(description = "第二个整数") int b) {
        return a + b;
    }


    /**
     * 工具3：模拟天气查询（调用外部API）
     */
    @Tool(description = "根据城市名查询天气")
    public String getWeather(
            @ToolParam(description = "城市名称，如北京、上海") String city) {
        // 实际项目中调用天气 API（如高德、和风）
        return city + "：晴朗，温度 22~28℃，微风";
    }

    /**
     * 工具4：计算两数之和（带参）
     * @ToolParam 描述参数，引导模型正确传参
     */
    @Tool(name = "webSearch",
            description = "通过搜索引擎获取实时信息，输入为搜索关键词，适用于查询新闻、天气、事件、知识等实时内容")
    public String search(
            @ToolParam(description = "搜索关键词，如2026年南昌天气、最新科技新闻") String query) {
        log.info("【自定义搜索工具】执行搜索，关键词：{}", query);
        return "姓名：蔡徐坤（KUN）\n" +
                "出生日期：1998 年 8 月 2 日\n" +
                "出生地：浙江省温州市\n" +
                "户籍：湖南吉首\n" +
                "身高：184cm\n" +
                "职业：中国内地男歌手、原创音乐制作人、MV 导演、演员\n" +
                "二、早年经历\n" +
                "5 岁开始接触舞台表演\n" +
                "初中毕业后赴美留学（格雷斯布莱恩斯中学），期间自学作曲\n" +
                "2012 年参加《向上吧！少年》进入全国 200 强，正式进入娱乐圈\n" +
                "2012 年参演首部电视剧《童话二分之一》\n" +
                "2014 年参演电影《完美假妻 168》";
    }
}
