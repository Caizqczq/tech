package com.mtm.backend.config;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG相关配置
 * 基于Spring AI最佳实践
 */
@Configuration
public class RAGConfig {

    /**
     * TokenTextSplitter配置
     * 基于Spring AI推荐的分块策略
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter(
            800,    // 分块大小：AI模型token限制的小百分比
            200,    // 重叠大小：保护语义边界
            5,      // 最小分块
            10000,  // 最大分块
            true    // 启用保留分隔符
        );
    }
}