package com.cj.imageagent.models;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MyReactAgent {

    private final ReactAgent reactAgent;

    public Flux<String> chat(String question, String conversationId) throws GraphRunnerException {
        // 1. 构建会话配置（threadId = 会话ID）
        RunnableConfig config = RunnableConfig.builder()
                .threadId(conversationId)
                .build();
        // 2. 用户消息
        UserMessage userMessage = new UserMessage(question);

        return reactAgent.stream(userMessage, config)
                .map(o -> (List<Message>)o.state().value("messages").get())
                .map(list->list.get(list.size()-1))
                .filter(AssistantMessage.class::isInstance)
                .map(m->((AssistantMessage)m).getText())
                .filter(s->!s.isBlank())
                .distinctUntilChanged();
    }
    public Flux<String> chatWithImagePath(String question, String imagePath , String conversationId) throws GraphRunnerException{

        // 1、构建resource资源
        File file = new File(imagePath);
        FileSystemResource resource = new FileSystemResource(file);
        // 2. 构建多模态 Media 对象，指定 MIME 类型（如 image/png）
        Media media = new Media(MimeTypeUtils.IMAGE_PNG, resource);
        // 3. 构建 UserMessage，将文本和图片合并
        UserMessage userMsg = UserMessage.builder()
                .text(question)
                .media(List.of(media))
                .build();
        // 4. 构建会话config
        RunnableConfig config = RunnableConfig.builder()
                .threadId(conversationId)
                .build();

        return reactAgent.stream(userMsg, config)
                .map(o -> (List<Message>)o.state().value("messages").get())
                .map(list->list.get(list.size()-1))
                .filter(AssistantMessage.class::isInstance)
                .map(m->((AssistantMessage)m).getText())
                .filter(s->!s.isBlank())
                .distinctUntilChanged();
    }
}
