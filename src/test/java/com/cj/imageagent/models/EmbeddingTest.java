package com.cj.imageagent.models;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class EmbeddingTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    @Qualifier("myVectorStore")
    private VectorStore vectorStore;

    /**
     * 测试1：测试向量化模型是否可用（nomic-embed-text）
     */
    @Test
    public void testEmbeddingModel() {
        String text = "这是一条测试文本，用于验证Ollama向量化功能";

        // 生成向量
        float[] embed = embeddingModel.embed(text);
        System.out.println("======================================");
        System.out.println("✅ 向量化模型调用成功！");
        int dim = embed.length;
        System.out.println("✅ 向量维度 = " + dim);
        // ========== 打印前10个数据 ==========
        System.out.print("✅ 向量前10个数据：[");
        for (int i = 0; i < 10; i++) {
            System.out.printf("%.6f", embed[i]);
            if (i < 9) System.out.print(", ");
        }
        System.out.println("]");

        if (dim == 768) {
            System.out.println("✅ 维度正确（nomic-embed-text = 768）");
        } else {
            System.out.println("❌ 维度错误！");
        }
        System.out.println("======================================");
    }


    /**
     * 测试2：测试向量库能否存入、检索（PostgreSQL + pgvector）
     */
    @Test
    public void testVectorStore() {
        // 1. 构造测试文档
        Document doc = new Document("测试：Spring AI + PostgreSQL 向量库");

        // 2. 存入向量库
        vectorStore.add(List.of(doc));
        System.out.println("======================================");
        System.out.println("✅ 文档已存入向量库");

        // 3. 相似度检索
        List<Document> result = vectorStore.similaritySearch("飞天茅台");

        System.out.println("✅ 检索到结果数量：" + result.size());
        System.out.println("✅ 检索内容：" + result.get(0).getText());
        System.out.println("======================================");
    }

    /**
     * 测试3：完整流程测试（最关键！）
     */
    @Test
    public void testFullRAGFlow() {
        System.out.println("======================================");
        System.out.println("🚀 开始完整 RAG 流程测试...");

        String testText = "我的配置是MySQL存聊天，PG存向量";

        // 向量化
        var embedding = embeddingModel.embed(testText);
        System.out.println("✅ 文本向量化成功，dim = " + embedding.length);

        // 存入
        Document doc = Document.builder()
                .text(testText)
                .build();
        vectorStore.add(List.of(doc));
        System.out.println("✅ 存入向量库成功");

        // 检索
        var searchResult = vectorStore.similaritySearch("我的配置");
        System.out.println("✅ 检索成功：" + searchResult.get(0).getText());

        System.out.println("🎉 🟢 全部测试通过！模型、向量化、向量库全部正常！");
        System.out.println("======================================");
    }
}