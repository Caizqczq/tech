package com.mtm.backend.service.rag;

import com.mtm.backend.model.DTO.RAGQueryDTO;
import com.mtm.backend.model.VO.RAGResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 对话服务 - 负责RAG对话和AI问答
 * 基于Spring AI ChatClient和Advisor最佳实践
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;

    /**
     * RAG对话 - 使用QuestionAnswerAdvisor
     */
    public RAGResponseVO chat(RAGQueryDTO queryDTO, Integer userId) {
        try {
            long startTime = System.currentTimeMillis();
            
            // 验证权限
            validateAccess(queryDTO.getKnowledgeBaseId(), userId);
            
            // 构建搜索请求
            SearchRequest searchRequest = createSearchRequest(queryDTO);
            
            // 使用QuestionAnswerAdvisor
            QuestionAnswerAdvisor ragAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build();
            
            // 构建时注册Advisor（推荐模式）
            ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(ragAdvisor)
                .build();
            
            // 构建提示词
            String prompt = buildPrompt(queryDTO);
            
            // 执行对话
            String answer = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
            
            // 获取参考文档
            List<Map<String, Object>> references = getReferences(queryDTO);
            
            // 生成相关问题
            List<String> relatedQuestions = generateRelatedQuestions(queryDTO.getQuery());
            
            double processingTime = (System.currentTimeMillis() - startTime) / 1000.0;
            
            return RAGResponseVO.builder()
                .answer(answer)
                .conversationId(queryDTO.getConversationId())
                .messageId(generateMessageId())
                .references(references)
                .relatedQuestions(relatedQuestions)
                .processingTime(processingTime)
                .timestamp(new Date())
                .build();
                
        } catch (Exception e) {
            log.error("RAG对话失败", e);
            throw new RuntimeException("RAG对话失败: " + e.getMessage());
        }
    }

    /**
     * 流式RAG对话
     */
    public Flux<String> chatStream(RAGQueryDTO queryDTO, Integer userId) {
        try {
            // 验证权限
            validateAccess(queryDTO.getKnowledgeBaseId(), userId);
            
            // 构建搜索请求
            SearchRequest searchRequest = createSearchRequest(queryDTO);
            
            // 使用QuestionAnswerAdvisor
            QuestionAnswerAdvisor ragAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build();
            
            ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(ragAdvisor)
                .build();
            
            String prompt = buildPrompt(queryDTO);
            
            // 返回流式响应
            return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
                
        } catch (Exception e) {
            log.error("流式RAG对话失败", e);
            return Flux.error(new RuntimeException("流式RAG对话失败: " + e.getMessage()));
        }
    }

    /**
     * 简单AI对话（不使用RAG）
     */
    public String simpleChat(String message) {
        try {
            ChatClient chatClient = chatClientBuilder.build();
            
            return chatClient.prompt()
                .user(message)
                .call()
                .content();
                
        } catch (Exception e) {
            log.error("简单对话失败", e);
            throw new RuntimeException("对话失败: " + e.getMessage());
        }
    }

    /**
     * 创建搜索请求
     */
    private SearchRequest createSearchRequest(RAGQueryDTO queryDTO) {
        try {
            SearchRequest.Builder searchBuilder = SearchRequest.builder()
                .query(queryDTO.getQuery())
                .topK(queryDTO.getTopK() != null ? queryDTO.getTopK() : 5)
                .similarityThreshold(0.7);  // Spring AI推荐阈值
            
            // 构建过滤条件
            if (queryDTO.getKnowledgeBaseId() != null) {
                log.debug("创建知识库过滤条件: knowledge_base_id = {}", queryDTO.getKnowledgeBaseId());
                
                FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();
                Filter.Expression filterExpression = filterBuilder
                    .eq("knowledge_base_id", queryDTO.getKnowledgeBaseId())
                    .build();
                searchBuilder.filterExpression(filterExpression);
                
                log.debug("过滤表达式: {}", filterExpression);
            }
            
            SearchRequest searchRequest = searchBuilder.build();
            log.debug("搜索请求创建成功: query={}, topK={}, threshold={}", 
                queryDTO.getQuery(), 
                queryDTO.getTopK() != null ? queryDTO.getTopK() : 5, 
                0.7);
            
            return searchRequest;
            
        } catch (Exception e) {
            log.error("创建搜索请求失败", e);
            throw new RuntimeException("搜索请求创建失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建提示词 - 根据回答模式调整
     */
    private String buildPrompt(RAGQueryDTO queryDTO) {
        StringBuilder promptBuilder = new StringBuilder();
        
        String answerMode = queryDTO.getAnswerMode() != null ? queryDTO.getAnswerMode() : "detailed";
        
        // 根据回答模式构建不同的提示词
        switch (answerMode) {
            case "concise":
                promptBuilder.append("请基于提供的上下文信息，用简洁明了的语言回答问题。回答要准确且直接，避免冗余信息。\n\n");
                break;
            case "tutorial":
                promptBuilder.append("请基于提供的上下文信息，以教学的方式详细解释答案。包括背景知识、步骤说明和实例，帮助学习者深入理解。\n\n");
                break;
            default:
                promptBuilder.append("请基于提供的上下文信息，详细且准确地回答问题。确保答案完整、逻辑清晰。\n\n");
                break;
        }
        
        promptBuilder.append("问题：").append(queryDTO.getQuery());
        
        return promptBuilder.toString();
    }

    /**
     * 获取参考文档信息
     */
    private List<Map<String, Object>> getReferences(RAGQueryDTO queryDTO) {
        try {
            if (queryDTO.getIncludeReferences() == null || !queryDTO.getIncludeReferences()) {
                return new ArrayList<>();
            }
            
            SearchRequest.Builder searchBuilder = SearchRequest.builder()
                .query(queryDTO.getQuery())
                .topK(3)  // 限制参考文档数量
                .similarityThreshold(0.7);
            
            if (queryDTO.getKnowledgeBaseId() != null) {
                FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();
                Filter.Expression filterExpression = filterBuilder
                    .eq("knowledge_base_id", queryDTO.getKnowledgeBaseId())
                    .build();
                searchBuilder.filterExpression(filterExpression);
            }
            
            List<Document> documents = vectorStore.similaritySearch(searchBuilder.build());
            
            return documents.stream()
                .map(doc -> {
                    Map<String, Object> reference = new HashMap<>();
                    reference.put("resourceId", doc.getMetadata().get("resource_id"));
                    reference.put("title", doc.getMetadata().get("title"));
                    reference.put("relevanceScore", calculateRelevanceScore(doc));
                    reference.put("excerpt", truncateText(doc.getText(), 200));
                    return reference;
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.warn("获取参考文档失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成相关问题
     */
    private List<String> generateRelatedQuestions(String query) {
        // TODO: 可以使用AI来生成更智能的相关问题
        return Arrays.asList(
            "能否提供更多相关的例子？",
            "这个概念在实际应用中如何使用？",
            "有哪些常见的误区需要注意？"
        );
    }

    /**
     * 验证知识库访问权限
     */
    private void validateAccess(String knowledgeBaseId, Integer userId) {
        if (knowledgeBaseId == null) {
            return; // 全局搜索，不需要特定权限验证
        }
        
        // TODO: 实现知识库权限验证逻辑
        // 可以注入KnowledgeBaseService或直接查询数据库
    }

    /**
     * 计算相关度评分
     */
    private double calculateRelevanceScore(Document document) {
        // Spring AI会在元数据中提供相似度分数
        Object distance = document.getMetadata().get("distance");
        if (distance instanceof Number) {
            // 将距离转换为相似度（1 - distance）
            return Math.max(0, 1.0 - ((Number) distance).doubleValue());
        }
        return 0.95; // 默认评分
    }

    /**
     * 截取文本
     */
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /**
     * 生成消息ID
     */
    private String generateMessageId() {
        return "msg_" + UUID.randomUUID().toString().replace("-", "");
    }
}