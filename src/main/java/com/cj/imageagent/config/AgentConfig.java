package com.cj.imageagent.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import com.cj.imageagent.Interceptor.ToolErrorInterceptor;
import com.cj.imageagent.models.MyReactAgent;
import com.cj.imageagent.tools.DateTool;
import com.cj.imageagent.tools.SearchTool;
import com.cj.imageagent.tools.WeatherTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import static com.cj.imageagent.prompt.SystemPrompt.RECT_SYSTEM_PROMPT;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class AgentConfig {

    private final DataSource dataSource;

    private final DateTool dateTool;
    private final SearchTool searchTool;
    private final WeatherTool weatherTool;


    @Bean
    public ReactAgent reactAgent(
            OllamaChatModel model,
            ChatHistorySaveHook chatHistorySaveHook
    ) {
        // 手动传入工具类！，不要采用自动扫描包逻辑！
        MethodToolCallbackProvider toolCallbackProvider = MethodToolCallbackProvider.builder()
                .toolObjects(dateTool, searchTool,weatherTool) // 手动写死
                .build();
        List<ToolCallback> allTools = List.of(toolCallbackProvider.getToolCallbacks());

        log.info("ReactAgent 最终成功加载工具数量: {}", allTools.size());


        // 配置Mysql checkpoint 存储，确保 Agent 的状态可以持久化
        MysqlSaver mysqlSaver = MysqlSaver.builder()
                .dataSource(dataSource)
                .build();

        return ReactAgent.builder()
                .name("PCB_Agent")
                .model(model)
                .systemPrompt(RECT_SYSTEM_PROMPT)
                .tools(allTools)
                .hooks(chatHistorySaveHook)
                .saver(mysqlSaver)
//                .instruction(RECT_INSTRUCTION)
                .interceptors(new ToolErrorInterceptor())
                .build();
    }
}

/**
 * 自动扫描所有Component中带有Tool注解的工具，但是由于扫描所有Component会导致循环依赖（比如Controller也会被扫描到）
 * //        Object[] toolCandidates = applicationContext.getBeansWithAnnotation(Component.class)
 * //                .values()
 * //                .stream()
 * //                // 扫描 工具类所在的包
 * //                .filter(bean-> bean.getClass().getPackageName().startsWith("com.cj.imageagent.tools"))
 * //                // 过滤掉没有 @Tool 注解方法的 Bean，避免抛出你刚才看到的错误
 * //                .filter(bean -> {
 * //                    Method[] methods = bean.getClass().getDeclaredMethods();
 * //                    return Arrays.stream(methods).anyMatch(m -> m.isAnnotationPresent(Tool.class));
 * //                })
 * //                .toArray();
 * //
 * //        if(toolCandidates.length==0) log.info("ReactAgent 没有找到任何工具，工具数量: 0");
 * //
 * //        // 2. 构建 Provider
 * //        MethodToolCallbackProvider toolCallbackProvider = MethodToolCallbackProvider.builder()
 * //                .toolObjects(toolCandidates)
 * //                .build();
 */
