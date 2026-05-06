package com.cj.imageagent.tools;


import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RAGTool {
    @Autowired
    @Qualifier("myVectorStore")
    private VectorStore vectorStore;

    public record Request(String query) {}
    public record Response(String content) {}

    /**
     * Agent 自动调用的知识库检索工具
     * @param request
     * @return
     */
    @Tool(name = "search_documents",description = "搜索知识库文档以查找相关信息")
    public Response search(@ToolParam(description = "用户的问题/查询关键词") Request request) {
        // 从向量存储检索相关文档
        List<Document> docs = vectorStore.similaritySearch(request.query());

        // 合并文档内容
        String combinedContent = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(""));
        return new Response(combinedContent);
    }

}
