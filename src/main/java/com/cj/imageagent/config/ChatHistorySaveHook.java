package com.cj.imageagent.config;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.cj.imageagent.Utils.MediaUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChatHistorySaveHook extends ModelHook{

    private final JdbcTemplate jdbcTemplate;
    private final MediaUtils mediaUtils;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "my_memory_hook";
    }

    @Override
    public HookPosition[] getHookPositions() {
        return new HookPosition[]{HookPosition.BEFORE_MODEL,HookPosition.AFTER_MODEL};
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        try{
            // 获取用户id
            String userId = config.threadId().orElse(null);
            // 访问消息历史
            Optional<Object> messagesOpt = state.value("messages");
            List<Message> messages = (List<Message>) messagesOpt.get();
            if (!messages.isEmpty()) {
                // 2. 获取【最后一条】= 用户最新提问  也有可能是Tool消息，需要进行判断
                Message lastMessage =  messages.get(messages.size() - 1);
                // 3. 保存到数据库（你只需要替换这里为真实 DB 插入）
                saveMessageToDB(lastMessage,userId);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        // 返回需要存入状态的数据（这里不需要新增，返回空）
        return CompletableFuture.completedFuture(Map.of());
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        try {
            // 获取用户id
            String userId = config.threadId().orElse(null);
            // 1. 从状态中获取 AI 返回的回答
            Optional<Object> messagesOpt = state.value("messages");
            if (messagesOpt.isPresent()) {
                List<Message> messages = (List<Message>) messagesOpt.get();
                if (!messages.isEmpty()) {
                    // 2. 最后一条 = AI 刚返回的回答
                    Message lastMsg = messages.get(messages.size() - 1);
                    if (lastMsg instanceof AssistantMessage assistantMessage) {
                        // 3. 保存 AI 回答到数据库
                        saveMessageToDB(assistantMessage,userId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(Map.of());
    }


    // ===================== 你自己实现的数据库保存方法 =====================
    /**
     * 保存用户输入到数据库
     */
    private void saveMessageToDB(Message lastMessage,String conversationId) {
        try {
            if(lastMessage instanceof UserMessage usermsg){
                // 是用户消息
                // 获取用户内容
                String content=usermsg.getText();
                // 获取用户类型
//                String type= String.valueOf(usermsg.getMessageType());
                String type= "user";
                // 获取用户信息附带的图片信息
                List<Media> mediaList  = usermsg.getMedia();
                List<String> base64Lists = mediaList.stream()
                        .map(media -> mediaUtils.convertMediaToBase64(media))
                        .collect(Collectors.toList());
                String mediaJson = objectMapper.writeValueAsString(base64Lists);

                jdbcTemplate.update(
                        "INSERT INTO chat_history (conversation_id, content, type, media_data) VALUES (?, ?, ?, ?)",
                        conversationId, content, type, mediaJson
                );
                log.info("存储用户消息成功:{}",usermsg.toString());
            }
            if(lastMessage instanceof AssistantMessage assistantMessage){
                // 有toolCalls = AI下发工具调用指令，内容是空，直接不保存！
                if(assistantMessage.getToolCalls() != null && !assistantMessage.getToolCalls().isEmpty()){
                    log.info("AI 调用工具：{}",assistantMessage.getToolCalls().get(0).name());
                    log.info("跳过工具调用Assistant消息，不入库");
                    return;
                }
                // 获取系统回复内容
                String content=assistantMessage.getText();
                // 获取系统类型
//                String type= String.valueOf(assistantMessage.getMessageType());
                String type="assistant";

                List<Media> mediaList  = assistantMessage.getMedia();
                List<String> base64Lists = mediaList.stream()
                        .map(media -> mediaUtils.convertMediaToBase64(media))
                        .collect(Collectors.toList());
                String mediaJson = objectMapper.writeValueAsString(base64Lists);

                jdbcTemplate.update(
                        "INSERT INTO chat_history (conversation_id, content, type, media_data) VALUES (?, ?, ?, ?)",
                        conversationId, content, type, mediaJson
                );
                log.info("存储系统消息成功:{}",assistantMessage.toString());
            }
            if(lastMessage instanceof ToolResponseMessage toolResponseMessage){
                // 是工具类型消息
                log.info("工具类型用户消息: {}",toolResponseMessage.getText());
            }
        }catch (Exception e){
            log.error("存储历史消息失败: {}", e.getMessage());
        }
    }
    private AssistantMessage preProcessAssistantMessage(AssistantMessage msg){
        String originalText = msg.getText();
        // 获取text中的urls
        Pattern urlPattern = Pattern.compile("https?://[^\\s)]+?\\.(png|jpg|jpeg|webp)(\\?[^\\s)]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = urlPattern.matcher(originalText);
        List<String> imageUrls  = new ArrayList<>();
        while (matcher.find()) {
            String url = matcher.group();
            imageUrls.add(url);
        }
        // ==============================
        // 2. 把URL转成Media
        // ==============================
        List<Media> oldMedia = msg.getMedia();
        List<Media> mediaList = new ArrayList<>();
        if (oldMedia != null && !oldMedia.isEmpty()) {
            mediaList.addAll(oldMedia);
        }
        for (String url : imageUrls) {
            mediaList.add(new Media(MimeTypeUtils.IMAGE_PNG, URI.create(url)));
        }

        // ==============================
        // 3. 复制并生成新的AssistantMessage
        // ==============================
        return AssistantMessage.builder()
                .content(msg.getText())
                .media(mediaList)
                .build();
    }
}
