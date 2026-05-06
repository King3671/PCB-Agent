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
import java.util.ArrayList;
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
    public Flux<String> chatWithImagePath(String question, List<String> imagePaths, String conversationId) throws GraphRunnerException{
        // 1、遍历所有图片路径，构建多个 Media 资源
        List<Media> mediaList = new ArrayList<>();
        for (String imagePath : imagePaths) {
            // 1.1、构建resource资源
            File file = new File(imagePath);
            FileSystemResource resource = new FileSystemResource(file);
            // 1.2. 构建多模态 Media 对象，指定 MIME 类型（如 image/png）
            Media media = new Media(MimeTypeUtils.IMAGE_PNG, resource);
            // 支持图片类型：png/jpg/jpeg/webp 都可以通用 IMAGE_PNG
            mediaList.add(media);
        }

        // 2. 构建 UserMessage，将文本和图片合并
        UserMessage userMsg = UserMessage.builder()
                .text(question)
                .media(mediaList)
                .build();
        // 3. 构建会话config
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
