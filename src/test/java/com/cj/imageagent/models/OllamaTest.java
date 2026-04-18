package com.cj.imageagent.models;

import com.cj.imageagent.entity.ChatHistory;
import com.cj.imageagent.entity.SpringAiChatMemory;
import com.cj.imageagent.mapper.ChatHistoryMapper;
import com.cj.imageagent.service.SpringAiChatMemoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class OllamaTest {

    @Autowired
    private Ollama ollama;

    @Autowired
    private SpringAiChatMemoryService memoryService;

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;

    @Test
    void testOllama() {
        // 定义唯一的会话 ID
        String conversationId = "test-user-002";

        // 第一轮对话：告诉 AI 名字
        String firstQuestion = "请通过浏览器查询蔡徐坤的简历";
        System.out.println("--- 第一轮对话 ---");

        // 使用 blockLast() 等待流结束，并观察输出
        ollama.chat(firstQuestion, conversationId)
                .doOnNext(System.out::print).blockLast();
    }


    @Test
    void testOllamaImage() {
        // 1. 定义唯一会话 ID
        String conversationId = "image-session-999";
        // 2. 准备一张本地图片路径
        String imagePath = "F:\\IdeaProject\\ImageAgent\\src\\main\\java\\com\\cj\\imageagent\\picture\\test1.jpg";

        System.out.println("--- 第一轮：发送图片并提问 ---");
        String firstQuestion = "请问图片内容是啥？";

        // 执行第一轮对话
        ollama.chatWithImagePath(firstQuestion,imagePath, conversationId)
                .doOnNext(System.out::print)
                .blockLast(); // 等待流结束

        System.out.println("\n\n--- 第二轮：不带图片询问历史内容 ---");
        // 如果记忆生效，AI 应该知道上一张图是猫
        String secondQuestion = "请问图片中的角色，右手拿的是什么东西？";

        ollama.chat(secondQuestion, conversationId) // 调用普通的带记忆 chat
                .doOnNext(System.out::print)
                .blockLast(); // 等待流结束

        System.out.println("\n--- 测试完成 ---");
    }

    @Test
    void testBase64() {
        // 1. 从数据库读取
        ChatHistory chatHistory = chatHistoryMapper.selectById("7");
        String rawJson = chatHistory.getMediaData(); // 这里拿到的应该是 ["data:image/png;base64,xxxx"]

        try {
            // 2. 使用 Jackson 解析 JSON 数组
            ObjectMapper mapper = new ObjectMapper();
            List<String> base64List = mapper.readValue(rawJson, new TypeReference<List<String>>() {});

            if (base64List.isEmpty()) {
                System.out.println("数据库中没有图片数据");
                return;
            }

            // 3. 获取第一张图片并去除前缀
            String fullBase64 = base64List.get(0);
            String pureBase64 = fullBase64;
            if (fullBase64.contains(",")) {
                pureBase64 = fullBase64.split(",")[1]; // 去掉 "data:image/png;base64," 只有后面的内容
            }

            // 4. 解码并写入文件
            byte[] imageBytes = java.util.Base64.getDecoder().decode(pureBase64);
            Files.write(Paths.get("F:/IdeaProject/ImageAgent/src/main/java/com/cj/imageagent/picture/output.jpg"), imageBytes);

            System.out.println("图片已成功从数据库导出并还原！请检查文件。");

        } catch (Exception e) {
            System.err.println("还原失败，原因：" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    void testMybatisPlus() {
        // 具体代码根据你的实体类和 Mapper 定义来写
        List<Map<String, Object>> historySessions = memoryService.getHistorySessions();
        // 遍历输出查询结果
        for (Map<String, Object> session : historySessions) {
            System.out.println("会话 ID: " + session.get("id") + ", 标题: " + session.get("title"));
        }
    }

    @Test
    void getId() {
        String id = "test-user-001";
        List<Map<String, String>> historyContent = memoryService.getHistoryContent(id);
        // 遍历输出查询结果
        for (Map<String, String> content : historyContent) {
            System.out.println("类型: " + content.get("type") + ", 内容: " + content.get("content"));
        }
    }
}
