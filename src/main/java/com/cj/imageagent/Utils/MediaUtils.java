package com.cj.imageagent.Utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class MediaUtils {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String convertMediaToBase64(Media media) {
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
    public List<Media> parseMediaJson(String mediaJson) {
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
