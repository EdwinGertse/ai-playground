package com.tegres.ai.playground.configuration;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.tegres.ai.playground.common.ComponentConstants.APP_PG_VECTOR_STORE;

@Configuration
public class RagConfiguration {

    @Bean(name = APP_PG_VECTOR_STORE)
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
