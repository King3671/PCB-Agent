package com.cj.imageagent.controller;

import com.cj.imageagent.entity.ChatHistory;
import com.cj.imageagent.entity.ChatMessageDTO;
import com.cj.imageagent.entity.SpringAiChatMemory;
import com.cj.imageagent.mapper.SpringAiChatMemoryMapper;
import com.cj.imageagent.models.Ollama;
import com.cj.imageagent.service.ChatHistoryService;
import com.cj.imageagent.service.SpringAiChatMemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class AgentController {

    private final Ollama ollama;
    private final ChatHistoryService service;

    // 只存 文件路径，不存字节！内存占用极小
    private final Map<String, String> imagePathCache = new HashMap<>();
    // 自定义临时目录，不会被Tomcat删除
    private final String TEMP_DIR = "F:\\IdeaProject\\ImageAgent\\src\\main\\java\\com\\cj\\imageagent\\uploadFile\\";

    /**
     * 1. 流式对话接口 - 自动判断是否需要带图分析
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @RequestParam String question,
            @RequestParam String conversationId,
            @RequestParam(required = false) Boolean hasImage // 新增：前端告知是否有图片
    ) {
        // 如果前端告知有图片，且缓存中存在路径
        if (Boolean.TRUE.equals(hasImage) && imagePathCache.containsKey("file")) {
            String imagePath = imagePathCache.get("file");

            // 注意：使用完后建议根据业务需求决定是否清除缓存，防止下一轮无图对话误触发
            // imagePathCache.remove("designImg");
            return ollama.chatWithImagePath(question, imagePath, conversationId);
        } else {
            // 普通文本对话
            return ollama.chat(question, conversationId);
        }
    }


    @GetMapping("/sessions")
    public List<Map<String, Object>> getSessions() {
        return service.getHistorySessions();
    }

    /**
     * 3. 获取指定会话的完整历史记录 (用于切换对话回显)
     */
    @GetMapping("/history/{conversationId}")
    public List<ChatMessageDTO> getHistory(@PathVariable String conversationId) {
        return service.getHistoryContent(conversationId);
    }

    /**
     * 4. 图片上传接口 - 修改返回值为 JSON 格式，方便前端处理
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadImages(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sessionId") String sessionId
    ) throws IOException {
        File dir = new File(TEMP_DIR);
        if (!dir.exists()) dir.mkdirs();

        String fileName = "Circuit_" + System.currentTimeMillis() + ".png";
        File saveFile = new File(TEMP_DIR + fileName);
        file.transferTo(saveFile);

        // 存入缓存，供 AI 读取（AI 需要本地绝对路径）
        imagePathCache.put("file", saveFile.getAbsolutePath());

        // 拼接返回给前端的 Web 访问地址
        String fileUrl = "http://localhost:8080/uploads/" + fileName;

        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("url", fileUrl); // 返回这个 URL
        res.put("msg", "上传成功");
        return res;
    }
}
