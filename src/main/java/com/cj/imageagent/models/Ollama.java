package com.cj.imageagent.models;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.io.File;

@Component
@RequiredArgsConstructor
public class Ollama {

    private final ChatClient chatClient;

    public Flux<String> chat(String question,String conversationId) {
        return chatClient.prompt()
                .user(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    // ===================== 图片对话 =====================
    /**
     * 带图片的流式对话（前端上传文件 MultipartFile）
     * @param question 文字问题
     * @param imagePath 前端上传的图片文件的字节数组，防止tomcat删除文件
     * @return 流式返回结果
     */
    public Flux<String> chatWithImagePath(String question, String imagePath , String conversationId) {
        File file = new File(imagePath);
        FileSystemResource resource = new FileSystemResource(file);

        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text(question)
                        .media(MimeTypeUtils.IMAGE_PNG, resource)
                )
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}
