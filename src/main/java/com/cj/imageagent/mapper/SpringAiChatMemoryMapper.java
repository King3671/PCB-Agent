package com.cj.imageagent.mapper;

import com.cj.imageagent.entity.SpringAiChatMemory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
* @author 王志诚
* @description 针对表【spring_ai_chat_memory】的数据库操作Mapper
* @createDate 2026-04-04 18:13:55
* @Entity com.cj.imageagent.entity.SpringAiChatMemory
*/
@Mapper
public interface SpringAiChatMemoryMapper extends BaseMapper<SpringAiChatMemory> {
    /**
     * 自定义 SQL 查询会话列表
     * 取每个会话的第一条用户消息内容作为标题
     */
    @Select("""
        SELECT conversation_id AS id, SUBSTRING(MIN(content), 1, 20) AS title FROM 
        spring_ai_chat_memory WHERE type = 'USER' GROUP BY conversation_id ORDER BY MIN(timestamp) DESC
    """)
    List<Map<String, Object>> getSessionList();

        /**
        * 获取指定会话的完整历史记录
        */
        List<SpringAiChatMemory> getHistorySessionList(String conversationId);
}




