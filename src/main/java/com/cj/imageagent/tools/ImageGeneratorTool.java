package com.cj.imageagent.tools;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Component
public class ImageGeneratorTool {

    private final RestTemplate restTemplate;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;
    private static final String DASHSCOPE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";

    public ImageGeneratorTool() {
        this.restTemplate = new RestTemplate();
    }

    @Tool(
            name = "gen_pcb_img",
            description = "生成PCB原理图图片，返回PCB原理图URL地址"
    )
    public String generateCircuitByWan27(
            @ToolParam(description = "请给出PCB原理图描述信息") String circuitDescription
    ) {
        // 1. 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // 2. 构造万相2.7官方请求体
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "wan2.7-image");

        // input.messages
        JSONObject input = new JSONObject();
        JSONObject message = new JSONObject();
        message.set("role", "user");

        JSONArray contentArray = new JSONArray();
        JSONObject textContent = new JSONObject();
        textContent.set("text", circuitDescription);
        contentArray.add(textContent);

        message.set("content", contentArray);
        JSONArray messagesArray = new JSONArray();
        messagesArray.add(message);
        input.set("messages", messagesArray);
        requestBody.set("input", input);

        // parameters
        JSONObject parameters = new JSONObject();
        parameters.set("enable_sequential", true);
        parameters.set("n", 1);
        parameters.set("size", "2k");
        requestBody.set("parameters", parameters);

        // ===================== 发送请求 =====================
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(
                DASHSCOPE_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        // ===================== 解析返回（纯 Hutool JSON） =====================
        String responseBody = response.getBody();
        // ===================== 解析 =====================
        JSONObject resultJson = new JSONObject(responseBody);
        // 提取图片下载URL（万相2.7标准返回结构）
        String url= resultJson
                .getJSONObject("output")
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getJSONArray("content")
                .getJSONObject(0)
                .getStr("image");
        System.out.println("AI生成图片URL地址为:" +url);
        return url;
    }
//        System.out.println("AI生成图片URL地址为:" +url);
////        String url="https://dashscope-7c2c.oss-accelerate.aliyuncs.com/1d/d0/20260427/437fa780/ad6e81dc-e33d-4d7d-8a35-42534a66a950_0.png?Expires=1777386003&OSSAccessKeyId=LTAI5tPxpiCM2hjmWrFXrym1&Signature=FjnwsYjUGDSedGAVMIPGoP8QvJI%3D";
//        return url;
//    }
/**
 * 可以下载的URL如下：
 * https://dashscope-7c2c.oss-accelerate.aliyuncs.com/1d/d0/20260427/437fa780/ad6e81dc-e33d-4d7d-8a35-42534a66a950_0.png?Expires=1777386003&OSSAccessKeyId=LTAI5tPxpiCM2hjmWrFXrym1&Signature=FjnwsYjUGDSedGAVMIPGoP8QvJI%3D
 */

//    public String TuregenerateCircuitByWan27(String circuitDescription) {
//        // 1. 请求头
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Bearer " + apiKey);
//
//        // 2. 构造万相2.7官方请求体
//        JSONObject requestBody = new JSONObject();
//        requestBody.set("model", "wan2.7-image");
//
//        // input.messages
//        JSONObject input = new JSONObject();
//        JSONObject message = new JSONObject();
//        message.set("role", "user");
//
//        JSONArray contentArray = new JSONArray();
//        JSONObject textContent = new JSONObject();
//        textContent.set("text", circuitDescription);
//        contentArray.add(textContent);
//
//        message.set("content", contentArray);
//        JSONArray messagesArray = new JSONArray();
//        messagesArray.add(message);
//        input.set("messages", messagesArray);
//        requestBody.set("input", input);
//
//        // parameters
//        JSONObject parameters = new JSONObject();
//        parameters.set("enable_sequential", true);
//        parameters.set("n", 1);
//        parameters.set("size", "2k");
//        requestBody.set("parameters", parameters);
//
//        // ===================== 发送请求 =====================
//        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
//        ResponseEntity<String> response = restTemplate.exchange(
//                DASHSCOPE_URL,
//                HttpMethod.POST,
//                requestEntity,
//                String.class
//        );
//
//        // ===================== 解析返回（纯 Hutool JSON） =====================
//        String responseBody = response.getBody();
//        // ===================== 解析 =====================
//        JSONObject resultJson = new JSONObject(responseBody);
//        // 提取图片下载URL（万相2.7标准返回结构）
//        String url= resultJson
//                .getJSONObject("output")
//                .getJSONArray("choices")
//                .getJSONObject(0)
//                .getJSONObject("message")
//                .getJSONArray("content")
//                .getJSONObject(0)
//                .getStr("image");
//        System.out.println("AI生成图片URL地址为:" +url);
//        return url;
//    }
}