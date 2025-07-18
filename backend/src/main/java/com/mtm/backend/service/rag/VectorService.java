package com.mtm.backend.service.rag;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 向量服务 - 负责文档向量化和向量存储
 * 基于Spring AI ETL Pipeline的Load阶段最佳实践
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VectorService {

    private final VectorStore vectorStore;
    private final DashScopeEmbeddingModel embeddingModel;

    /**
     * Load: 批量存储文档到向量数据库
     * 采用Spring AI推荐的批处理策略
     */
    public void storeDocuments(List<Document> documents, String knowledgeBaseId) {
        try {
            log.info("开始向量化存储 {} 个文档，知识库: {}", documents.size(), knowledgeBaseId);
            
            // 为所有文档添加知识库标识
            documents.forEach(doc -> 
                doc.getMetadata().put("knowledge_base_id", knowledgeBaseId)
            );
            
            // 批量处理策略：避免过大的批次
            int batchSize = 10;
            int totalProcessed = 0;
            
            for (int i = 0; i < documents.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, documents.size());
                List<Document> batch = documents.subList(i, endIndex);
                
                try {
                    // Spring AI VectorStore自动处理嵌入计算
                    vectorStore.add(batch);
                    
                    totalProcessed += batch.size();
                    int progress = (totalProcessed * 100) / documents.size();
                    
                    log.info("向量化进度: {}/{} ({}%)", totalProcessed, documents.size(), progress);
                    
                    // 批次间延迟，避免API调用过频
                    if (i + batchSize < documents.size()) {
                        Thread.sleep(200);
                    }
                    
                } catch (Exception e) {
                    log.error("批次向量化失败 {}-{}: {}", i, endIndex, e.getMessage());
                    throw new RuntimeException("向量化失败", e);
                }
            }
            
            log.info("向量化存储完成，总计: {} 个文档", totalProcessed);
            
        } catch (Exception e) {
            log.error("向量化存储失败，知识库: {}", knowledgeBaseId, e);
            throw new RuntimeException("向量化存储失败", e);
        }
    }

    /**
     * 单个文档向量化（用于增量更新）
     */
    public void storeDocument(Document document, String knowledgeBaseId) {
        document.getMetadata().put("knowledge_base_id", knowledgeBaseId);
        vectorStore.add(List.of(document));
        log.debug("单个文档向量化完成: {}", document.getMetadata().get("id"));
    }

    /**
     * 删除向量数据
     */
    public void deleteVectors(List<String> documentIds) {
        try {
            vectorStore.delete(documentIds);
            log.info("删除向量数据: {} 个文档", documentIds.size());
        } catch (Exception e) {
            log.error("删除向量数据失败", e);
            throw new RuntimeException("删除向量数据失败", e);
        }
    }

    /**
     * 删除知识库所有向量
     */
    public void deleteKnowledgeBaseVectors(String knowledgeBaseId) {
        try {
            // 注意：这里需要根据VectorStore的具体实现来删除
            // Redis VectorStore可能需要特殊处理
            log.info("删除知识库向量数据: {}", knowledgeBaseId);
            
            // TODO: 实现批量删除逻辑
            // 可能需要先查询所有相关文档ID，然后批量删除
            
        } catch (Exception e) {
            log.error("删除知识库向量失败: {}", knowledgeBaseId, e);
            throw new RuntimeException("删除知识库向量失败", e);
        }
    }

    /**
     * 直接文本嵌入（用于查询向量化）
     * 增加重试机制保证稳定性
     */
    @Retryable(
        value = {RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 8000)
    )
    public EmbeddingResponse embedText(String text) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("开始文本向量化，长度: {}", text.length());
            
            EmbeddingRequest request = new EmbeddingRequest(List.of(text), null);
            EmbeddingResponse response = embeddingModel.call(request);
            
            long duration = System.currentTimeMillis() - startTime;
            log.debug("文本向量化完成，耗时: {}ms", duration);
            
            return response;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("文本向量化失败，耗时: {}ms，错误: {}", duration, e.getMessage());
            throw new RuntimeException("文本向量化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 异步文本嵌入
     */
    public CompletableFuture<EmbeddingResponse> embedTextAsync(String text) {
        return CompletableFuture.supplyAsync(() -> embedText(text));
    }

    /**
     * 连接健康检查
     */
    public boolean isHealthy() {
        try {
            String testText = "连接测试";
            EmbeddingResponse response = embedText(testText);
            
            boolean healthy = response != null && 
                            response.getResults() != null && 
                            !response.getResults().isEmpty();
            
            log.debug("向量服务健康检查: {}", healthy ? "正常" : "异常");
            return healthy;
            
        } catch (Exception e) {
            log.error("向量服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取向量存储统计信息
     */
    public VectorStoreStats getStats() {
        // TODO: 根据VectorStore实现获取统计信息
        return VectorStoreStats.builder()
            .totalDocuments(0L)
            .totalVectors(0L)
            .build();
    }

    /**
     * 向量存储统计信息
     */
    @lombok.Builder
    @lombok.Data
    public static class VectorStoreStats {
        private Long totalDocuments;
        private Long totalVectors;
        private String storageType;
        private Long usedMemory;
    }
}