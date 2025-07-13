package com.mtm.backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class ModelConfig {
    private final ChatModel chatModel;
    private final EmbeddingModel embeddingModel;

    public ModelConfig(ChatModel chatModel, EmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    @Bean
    public ChatClient chatClient(){
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * 配置Redis VectorStore Bean
     * 基于Spring AI官方文档的标准配置方式
     */
    @Bean
    public VectorStore vectorStore(JedisPooled jedisPooled) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("teaching-resources")           // 与application.yml中的配置保持一致
                .prefix("vector:")                         // 与application.yml中的配置保持一致
                .initializeSchema(true)                    // 自动初始化Redis索引结构
                .build();
    }

    /**
     * 配置JedisPooled Bean用于Redis连接
     * 基于Spring AI官方文档的推荐配置
     */
    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled("localhost", 6379);
    }
}
