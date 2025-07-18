package com.mtm.backend.service.rag;

import com.mtm.backend.model.DTO.RAGQueryDTO;
import com.mtm.backend.model.VO.RAGResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * RAG门面服务 - 统一的RAG对外接口
 * 基于Spring AI最佳实践，整合各个专业服务
 * 
 * 这是Controller层应该调用的唯一RAG服务接口
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RAGFacadeService {

    private final ChatService chatService;
    private final SearchService searchService;
    private final VectorService vectorService;

    /**
     * RAG对话 - 主要对外接口
     */
    public RAGResponseVO query(RAGQueryDTO queryDTO, Integer userId) {
        try {
            log.info("RAG查询请求: 用户={}, 知识库={}, 问题={}", 
                userId, queryDTO.getKnowledgeBaseId(), queryDTO.getQuery());
            
            return chatService.chat(queryDTO, userId);
            
        } catch (Exception e) {
            log.error("RAG查询失败", e);
            throw new RuntimeException("RAG查询失败: " + e.getMessage());
        }
    }

    /**
     * 流式RAG对话
     */
    public Flux<String> queryStream(RAGQueryDTO queryDTO, Integer userId) {
        try {
            log.info("RAG流式查询请求: 用户={}, 知识库={}", 
                userId, queryDTO.getKnowledgeBaseId());
            
            return chatService.chatStream(queryDTO, userId);
            
        } catch (Exception e) {
            log.error("RAG流式查询失败", e);
            return Flux.error(new RuntimeException("RAG流式查询失败: " + e.getMessage()));
        }
    }

    /**
     * 简单AI对话（不使用RAG）
     */
    public String simpleQuery(String message) {
        try {
            return chatService.simpleChat(message);
            
        } catch (Exception e) {
            log.error("简单对话失败", e);
            throw new RuntimeException("对话失败: " + e.getMessage());
        }
    }

    /**
     * 语义搜索 - 不生成回答，只返回相关文档
     */
    public Object semanticSearch(String query, String knowledgeBaseId, Integer userId, int topK, double threshold) {
        try {
            log.info("语义搜索请求: 用户={}, 知识库={}, 查询={}", userId, knowledgeBaseId, query);
            
            // 使用SearchService进行搜索
            SearchService.SearchCriteria criteria = SearchService.SearchCriteria.builder()
                .query(query)
                .knowledgeBaseId(knowledgeBaseId)
                .userId(userId)
                .topK(topK)
                .threshold(threshold)
                .build();
            
            var documents = searchService.advancedSearch(criteria);
            
            // 转换为前端需要的格式
            return documents.stream()
                .map(doc -> {
                    var metadata = doc.getMetadata();
                    return Map.of(
                        "id", metadata.get("id"),
                        "title", metadata.getOrDefault("title", ""),
                        "content", doc.getText(),
                        "source", metadata.getOrDefault("source", ""),
                        "subject", metadata.getOrDefault("subject", ""),
                        "similarity", calculateSimilarity(doc),
                        "resourceId", metadata.get("resource_id")
                    );
                })
                .collect(java.util.stream.Collectors.toList());
                
        } catch (Exception e) {
            log.error("语义搜索失败", e);
            throw new RuntimeException("语义搜索失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    public boolean isHealthy() {
        try {
            return vectorService.isHealthy();
        } catch (Exception e) {
            log.error("RAG服务健康检查失败", e);
            return false;
        }
    }

    /**
     * 获取服务统计信息
     */
    public Object getStats() {
        try {
            var vectorStats = vectorService.getStats();
            
            return Map.of(
                "vectorStore", vectorStats,
                "serviceStatus", "healthy",
                "timestamp", new java.util.Date()
            );
            
        } catch (Exception e) {
            log.error("获取RAG统计信息失败", e);
            return Map.of(
                "serviceStatus", "error",
                "error", e.getMessage(),
                "timestamp", new java.util.Date()
            );
        }
    }

    // ============ 私有工具方法 ============

    private double calculateSimilarity(org.springframework.ai.document.Document document) {
        Object distance = document.getMetadata().get("distance");
        if (distance instanceof Number) {
            return Math.max(0, 1.0 - ((Number) distance).doubleValue());
        }
        return 0.95;
    }
}