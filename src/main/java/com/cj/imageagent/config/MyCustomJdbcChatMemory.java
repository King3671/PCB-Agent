package com.cj.imageagent.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MyCustomJdbcChatMemory implements ChatMemory {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void add(String conversationId, List<Message> messages) {
        for (Message message : messages) {
            try {
                String content = message.getText();
                String type = message.getMessageType().getValue();
                String mediaJson = "[]";

                if (message instanceof UserMessage userMessage && userMessage.getMedia() != null) {
                    List<String> base64Strings = new ArrayList<>();
                    for (Media media : userMessage.getMedia()) {
                        String base64 = convertMediaToBase64(media);
                        if (base64 != null) {
                            base64Strings.add(base64);
                        }
                    }
                    mediaJson = objectMapper.writeValueAsString(base64Strings);
                }

                jdbcTemplate.update(
                        "INSERT INTO chat_history (conversation_id, content, type, media_data) VALUES (?, ?, ?, ?)",
                        conversationId, content, type, mediaJson
                );
            } catch (Exception e) {
                log.error("存储历史消息失败: {}", e.getMessage());
            }
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        // 查询时，需要把 media_data 读出来重新封装成 UserMessage 的 Media 列表
        return jdbcTemplate.query(
                "SELECT content, type, media_data FROM chat_history WHERE conversation_id = ? ORDER BY timestamp ASC",
                (rs, rowNum) -> {
                    String type = rs.getString("type");
                    String content = rs.getString("content");
                    String mediaJson = rs.getString("media_data");// 拿到你存的 Base64 JSON

                    if ("USER".equals(type)) {
                        List<Media> mediaList = parseMediaJson(mediaJson);
                        // 使用 Builder 模式创建 UserMessage
                        return UserMessage.builder()
                                .text(content)
                                .media(mediaList)
                                .build();
                    } else {
                        return new AssistantMessage(content);
                    }
                },
                conversationId
        );
    }

    @Override
    public void clear(String conversationId) {
        jdbcTemplate.update("DELETE FROM chat_history WHERE conversation_id = ?", conversationId);
    }

    // ================= 辅助工具方法 =================

    /**
     * 将 Media 对象（无论是 FileSystemResource 还是 Base64）统一转为 Base64 字符串
     */
    private String convertMediaToBase64(Media media) {
        try {
            Object data = media.getData();
            // 如果是文件资源，读取并转换
            if (data instanceof org.springframework.core.io.Resource resource) {
                byte[] bytes = org.springframework.util.StreamUtils.copyToByteArray(resource.getInputStream());
                String base64Content = java.util.Base64.getEncoder().encodeToString(bytes);
                return "data:" + media.getMimeType().toString() + ";base64," + base64Content;
            }
            // 情况 B: 数据已经是 byte[] (某些内部实现会返回字节数组)
            if (data instanceof byte[] bytes) {
                String base64Content = java.util.Base64.getEncoder().encodeToString(bytes);
                return "data:" + media.getMimeType().toString() + ";base64," + base64Content;
            }
            // 情况 C: 如果已经是 String，直接返回
            return data.toString();
        } catch (Exception e) {
            log.error("Media 转换 Base64 失败", e);
            return null;
        }
    }

    /**
     * 将数据库存的 JSON 还原为 Media 列表
     */
    private List<Media> parseMediaJson(String mediaJson) {
        if (mediaJson == null || mediaJson.isEmpty() || "[]".equals(mediaJson)) {
            return Collections.emptyList();
        }
        try {
            List<String> base64List = objectMapper.readValue(mediaJson, new TypeReference<List<String>>() {});
            return base64List.stream()
                    .map(base64 -> {
                        // 如果构造函数报错，说明它需要一个 Resource 对象
                        // 我们将 Base64 字符串包装成 ByteArrayResource
                        byte[] decodedBytes = Base64.getDecoder().decode(
                                base64.contains(",") ? base64.split(",")[1] : base64
                        );
                        org.springframework.core.io.ByteArrayResource resource =
                                new org.springframework.core.io.ByteArrayResource(decodedBytes);

                        return new Media(MimeTypeUtils.IMAGE_PNG, resource);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("解析 Media JSON 失败", e);
            return Collections.emptyList();
        }
    }

}
