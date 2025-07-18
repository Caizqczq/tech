package com.mtm.backend.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索服务 - 负责向量相似性搜索和文档检索
 * 基于Spring AI VectorStore最佳实践
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final VectorStore vectorStore;

    /**
     * 语义搜索 - 知识库范围
     */
    public List<Document> semanticSearch(String query, String knowledgeBaseId, int topK, double threshold) {
        try {
            log.debug("语义搜索: query={}, knowledgeBaseId={}, topK={}", query, knowledgeBaseId, topK);
            
            SearchRequest.Builder searchBuilder = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold);
            
            // 使用FilterExpressionBuilder构建过滤条件
            if (knowledgeBaseId != null) {
                searchBuilder.filterExpression("knowledge_base_id == '" + knowledgeBaseId + "'");
            }
            
            List<Document> results = vectorStore.similaritySearch(searchBuilder.build());
            
            log.info("语义搜索完成，找到 {} 个相关文档", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("语义搜索失败", e);
            throw new RuntimeException("语义搜索失败: " + e.getMessage());
        }
    }

    /**
     * 高级语义搜索 - 支持多条件过滤
     */
    public List<Document> advancedSearch(SearchCriteria criteria) {
        try {
            log.debug("高级语义搜索: {}", criteria);
            
            SearchRequest.Builder searchBuilder = SearchRequest.builder()
                .query(criteria.getQuery())
                .topK(criteria.getTopK())
                .similarityThreshold(criteria.getThreshold());
            
            // 构建复合过滤条件
            List<String> filterConditions = new ArrayList<>();
            
            // 知识库过滤
            if (criteria.getKnowledgeBaseId() != null) {
                filterConditions.add("knowledge_base_id == '" + criteria.getKnowledgeBaseId() + "'");
            }
            
            // 用户权限过滤
            if (criteria.getUserId() != null) {
                filterConditions.add("user_id == " + criteria.getUserId());
            }
            
            // 学科过滤
            if (criteria.getSubject() != null) {
                filterConditions.add("subject == '" + criteria.getSubject() + "'");
            }
            
            // 课程层次过滤
            if (criteria.getCourseLevel() != null) {
                filterConditions.add("course_level == '" + criteria.getCourseLevel() + "'");
            }
            
            // 文档类型过滤
            if (criteria.getDocumentType() != null) {
                filterConditions.add("document_type == '" + criteria.getDocumentType() + "'");
            }
            
            // 组合过滤条件
            if (!filterConditions.isEmpty()) {
                String combinedFilter = String.join(" && ", filterConditions);
                searchBuilder.filterExpression(combinedFilter);
            }
            
            List<Document> results = vectorStore.similaritySearch(searchBuilder.build());
            
            log.info("高级搜索完成，找到 {} 个相关文档", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("高级搜索失败", e);
            throw new RuntimeException("高级搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取知识库中的随机文档样本
     */
    public List<Document> getRandomSamples(String knowledgeBaseId, int sampleSize) {
        try {
            // 使用通用查询获取更多文档
            SearchRequest searchRequest = SearchRequest.builder()
                .query("*")  // 通配符查询
                .topK(sampleSize * 3)  // 获取更多结果用于随机选择
                .similarityThreshold(0.0)  // 降低阈值
                .build();
            
            if (knowledgeBaseId != null) {
                searchRequest = SearchRequest.builder()
                    .query("*")
                    .topK(sampleSize * 3)
                    .similarityThreshold(0.0)
                    .filterExpression("knowledge_base_id == '" + knowledgeBaseId + "'")
                    .build();
            }
            
            List<Document> allDocs = vectorStore.similaritySearch(searchRequest);
            
            // 随机采样
            Collections.shuffle(allDocs);
            return allDocs.stream()
                .limit(sampleSize)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("获取随机样本失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据文档ID批量获取文档
     */
    public List<Document> getDocumentsByIds(List<String> documentIds) {
        try {
            // 注意：这里需要根据VectorStore的具体实现
            // 目前Spring AI VectorStore接口没有直接的ID查询方法
            // 可能需要通过元数据过滤来实现
            
            List<Document> results = new ArrayList<>();
            
            for (String docId : documentIds) {
                SearchRequest searchRequest = SearchRequest.builder()
                    .query("*")
                    .topK(1)
                    .similarityThreshold(0.0)
                    .filterExpression("id == '" + docId + "'")
                    .build();
                
                List<Document> docs = vectorStore.similaritySearch(searchRequest);
                results.addAll(docs);
            }
            
            return results;
            
        } catch (Exception e) {
            log.error("根据ID获取文档失败", e);
            throw new RuntimeException("获取文档失败: " + e.getMessage());
        }
    }

    /**
     * 搜索条件封装类
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class SearchCriteria {
        private String query;
        private String knowledgeBaseId;
        private Integer userId;
        private String subject;
        private String courseLevel;
        private String documentType;
        private int topK = 10;
        private double threshold = 0.7;
        
        @Override
        public String toString() {
            return String.format("SearchCriteria{query='%s', knowledgeBaseId='%s', topK=%d, threshold=%.2f}", 
                query, knowledgeBaseId, topK, threshold);
        }
    }

    /**
     * 搜索结果统计
     */
    @lombok.Data
    @lombok.Builder
    public static class SearchStats {
        private int totalResults;
        private double avgSimilarity;
        private double maxSimilarity;
        private double minSimilarity;
        private long searchTimeMs;
    }
}