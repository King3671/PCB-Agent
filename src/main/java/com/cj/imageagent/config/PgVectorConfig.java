package com.cj.imageagent.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;


@Configuration
public class PgVectorConfig {

    @Bean("myVectorStore")
    public VectorStore myVectorStore(
            @Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel
    ) {

        return PgVectorStore.builder(jdbcTemplate,embeddingModel)
                .vectorTableName("rag_vector_store")
                .dimensions(768) // nomic固定768
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE) // 余弦距离
                .initializeSchema(true)
                .build();
    }

}