package com.cj.imageagent.service;

import com.cj.imageagent.entity.SpringAiChatMemory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author 王志诚
* @description 针对表【spring_ai_chat_memory】的数据库操作Service
* @createDate 2026-04-04 18:13:55
*/
public interface SpringAiChatMemoryService extends IService<SpringAiChatMemory> {
    List<Map<String, Object>> getHistorySessions();
    List<Map<String,String>> getHistoryContent(String conversationId);
}
