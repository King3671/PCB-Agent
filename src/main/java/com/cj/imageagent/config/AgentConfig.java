package com.cj.imageagent.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import com.cj.imageagent.Interceptor.ToolErrorInterceptor;
import com.cj.imageagent.tools.DateTool;
import com.cj.imageagent.tools.ImageGeneratorTool;
import com.cj.imageagent.tools.RAGTool;
import com.cj.imageagent.tools.SearchTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

import static com.cj.imageagent.prompt.SystemPrompt.RECT_INSTRUCTION;
import static com.cj.imageagent.prompt.SystemPrompt.RECT_SYSTEM_PROMPT;

@Slf4j
@Configuration
public class AgentConfig {

    private final DataSource dataSource;
    private final DateTool dateTool;
    private final SearchTool searchTool;
    private final ImageGeneratorTool imageGeneratorTool;
    private final RAGTool ragTool;

    // 👇 关键！构造方法参数上加 @Qualifier！
    public AgentConfig(
            @Qualifier("mysqlDataSource") DataSource dataSource,
            DateTool dateTool,
            SearchTool searchTool,
            ImageGeneratorTool imageGeneratorTool,
            RAGTool ragTool
    ) {
        this.dataSource = dataSource;
        this.dateTool = dateTool;
        this.searchTool = searchTool;
        this.imageGeneratorTool = imageGeneratorTool;
        this.ragTool=ragTool;
    }


    @Bean
    public ReactAgent reactAgent(
            OllamaChatModel model,
            ChatHistorySaveHook chatHistorySaveHook
    ) {
        // 手动传入工具类！，不要采用自动扫描包逻辑！
        MethodToolCallbackProvider toolCallbackProvider = MethodToolCallbackProvider.builder()
                .toolObjects(dateTool, searchTool,imageGeneratorTool,ragTool) // 手动写死
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
                .instruction(RECT_INSTRUCTION)
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
