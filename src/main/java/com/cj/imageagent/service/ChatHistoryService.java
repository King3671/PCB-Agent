package com.cj.imageagent.service;

import com.cj.imageagent.entity.ChatHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cj.imageagent.entity.ChatMessageDTO;

import java.util.List;
import java.util.Map;

/**
* @author 王志诚
* @description 针对表【chat_history】的数据库操作Service
* @createDate 2026-04-11 17:15:43
*/
public interface ChatHistoryService extends IService<ChatHistory> {
    List<Map<String, Object>> getHistorySessions();
    List<ChatMessageDTO> getHistoryContent(String conversationId);

}
