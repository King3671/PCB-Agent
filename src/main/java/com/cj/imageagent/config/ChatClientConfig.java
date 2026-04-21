package com.cj.imageagent.config;

import com.cj.imageagent.prompt.SystemPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    @Bean
    public ChatClient chatClient(
            OllamaChatModel model,
            MyCustomJdbcChatMemory customMemory
//            WeatherForLocationTool weatherTool
    ) {
        return ChatClient.builder(model)
                .defaultSystem(SystemPrompt.SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(customMemory)
                                .build()
                )
//                .defaultTools(weatherTool)
                .build();
    }
}
