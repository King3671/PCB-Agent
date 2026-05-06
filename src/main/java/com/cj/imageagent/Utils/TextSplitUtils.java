package com.cj.imageagent.Utils;

import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TextSplitUtils {
    // 嵌入模型最大字符数（根据你的模型改）
    private static final int MAX_CHAR_LENGTH = 380;
    // 重叠字符
    private static final int OVERLAP_CHAR_LENGTH = 60;

    /**
     * 清洗文本：去除多余空格、空行、制表符
     */
    private static String cleanWhitespace(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text
                .replaceAll("[\\s　]+", " ")
                .trim()
                .replaceAll(" \n ", "\n")
                .replaceAll("\n{2,}", "\n");
    }

    /**
     * 安全的字符分割（修复负数越界）
     */
    public static List<Document> splitByCharacterCount(List<Document> documents) {
        List<Document> splitDocuments = new ArrayList<>();

        for (Document doc : documents) {
            String rawContent = doc.getText();
            String cleanContent = cleanWhitespace(rawContent);

            if (cleanContent.isBlank()) continue;

            int length = cleanContent.length();
            int start = 0;

            while (start < length) {
                int end = Math.min(start + MAX_CHAR_LENGTH, length);
                String chunk = cleanContent.substring(start, end);

                // 保留元数据
                Document splitDoc = new Document(chunk, new HashMap<>(doc.getMetadata()));
                splitDocuments.add(splitDoc);

                // ====================== 修复点：防止 start 变成负数 ======================
                int nextStart = end - OVERLAP_CHAR_LENGTH;
                if (nextStart <= start) {
                    // 无法再重叠了，直接结束
                    break;
                }
                start = nextStart;
            }
        }
        return splitDocuments;
    }
}
