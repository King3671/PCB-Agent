package com.cj.imageagent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cj.imageagent.entity.ChatHistory;
import com.cj.imageagent.entity.ChatMessageDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.cj.imageagent.service.ChatHistoryService;
import com.cj.imageagent.mapper.ChatHistoryMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 王志诚
* @description 针对表【chat_history】的数据库操作Service实现
* @createDate 2026-04-11 17:15:43
*/
@RequiredArgsConstructor
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
    implements ChatHistoryService{

    private final ChatHistoryMapper chatHistoryMapper;
    @Override
    public List<Map<String, Object>> getHistorySessions() {
        return chatHistoryMapper.getSessionList();
    }

    @Override
    public List<ChatMessageDTO> getHistoryContent(String conversationId) {
        List<ChatHistory> historys = chatHistoryMapper.getHistorySessionList(conversationId);
        ObjectMapper objectMapper = new ObjectMapper();
        return historys.stream()
                .map(
                        history->{
                            // 采用ChatMessageDTO的格式
                            ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
                            BeanUtil.copyProperties(history, chatMessageDTO);
                            // 解析 Media_data (JSON String -> List<String>)
                            String rawMediaJson = history.getMediaData(); // 假设数据库实体类对应字段名为 mediaData
                            try {
                                if (rawMediaJson != null && !rawMediaJson.isEmpty()) {
                                    // 将 JSON 字符串反序列化为 List<String>
                                    List<String> mediaList = objectMapper.readValue(
                                            rawMediaJson,
                                            new TypeReference<List<String>>() {}
                                    );
                                    chatMessageDTO.setMedia_data(mediaList);
                                } else {
                                    chatMessageDTO.setMedia_data(Collections.emptyList());
                                }
                            } catch (Exception e) {
                                // 打印错误日志并给一个空列表，防止前端因为 null 崩溃
                                System.err.println("解析历史记录中的图片数据失败: " + e.getMessage());
                                chatMessageDTO.setMedia_data(Collections.emptyList());
                            }
                            return chatMessageDTO;
                        }
                )
                .collect(Collectors.toList());
    }
}




