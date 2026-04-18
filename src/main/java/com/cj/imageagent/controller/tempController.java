package com.cj.imageagent.controller;
import com.cj.imageagent.models.Ollama;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/a/temp")
public class tempController {

    private final Ollama ollama;

    // 只存 文件路径，不存字节！内存占用极小
    private final Map<String, String> imagePathCache = new HashMap<>();
    // 自定义临时目录，不会被Tomcat删除
    private final String TEMP_DIR = "F:\\IdeaProject\\ImageAgent\\src\\main\\java\\com\\cj\\imageagent\\uploadFile\\";

    // 第一次上传：
    @PostMapping("/upload")
    public String uploadImages(
            @RequestParam("designImg") MultipartFile designImg
    ) throws IOException {
        // 生成唯一文件名
        String fileName = "Circuit_" + System.currentTimeMillis() + ".png";
        File saveFile = new File(TEMP_DIR + fileName);
        // 保存到自定义临时目录
        designImg.transferTo(saveFile);
        // 只缓存路径！超级省内存
        imagePathCache.put("designImg", saveFile.getAbsolutePath());
        return "上传成功！可以开始对话了";
    }

    @PostMapping("/chat")
    public Flux<String> chat(@RequestParam("question") String question) {
        String imgPath = imagePathCache.get("designImg");
        if (imgPath == null ) {
            return Flux.just("请先上传电路图！");
        }
        try {
            return ollama.chatWithImagePath(question, imgPath,"");
        } catch (Exception e) {
            return Flux.just("AI分析异常：" + e.getMessage());
        }
    }
}
