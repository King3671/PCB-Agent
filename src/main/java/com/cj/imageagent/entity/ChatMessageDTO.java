package com.cj.imageagent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    String type;          // user, assistant, system
    String content;          // 文字内容
    List<String> media_data ;// 图片的 Base64 字符串或 URL 列表
}
