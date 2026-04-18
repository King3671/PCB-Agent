package com.cj.imageagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cj.imageagent.entity.SpringAiChatMemory;
import com.cj.imageagent.service.SpringAiChatMemoryService;
import com.cj.imageagent.mapper.SpringAiChatMemoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 王志诚
* @description 针对表【spring_ai_chat_memory】的数据库操作Service实现
* @createDate 2026-04-04 18:13:55
*/
@Service
@RequiredArgsConstructor
public class SpringAiChatMemoryServiceImpl extends ServiceImpl<SpringAiChatMemoryMapper, SpringAiChatMemory>
    implements SpringAiChatMemoryService{

    private final SpringAiChatMemoryMapper springAiChatMemoryMapper;

    @Override
    public List<Map<String, Object>> getHistorySessions() {
        return springAiChatMemoryMapper.getSessionList();
    }

    @Override
    public List<Map<String, String>> getHistoryContent(String conversationId) {
        List<SpringAiChatMemory> historys = springAiChatMemoryMapper.getHistorySessionList(conversationId);

        return historys.stream()
                .map(
                        history->{
                            Map<String, String> map = new HashMap<>();
                            // .toLowerCase() 是为了兼容你前端 Vue 代码中的 msg.type === 'ai' 或 'user'
                            map.put("type", ((String) history.getType()).equals("USER")?"user":"ai");
                            map.put("content", history.getContent());
                            return map;
                        }
                )
                .collect(Collectors.toList());
    }

}




