package com.mtm.backend.service;

import com.mtm.backend.model.DTO.RAGQueryDTO;
import com.mtm.backend.model.VO.RAGResponseVO;
import com.mtm.backend.repository.KnowledgeBase;
import com.mtm.backend.repository.mapper.KnowledgeBaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG智能问答服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RAGService {
    
    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    
    /**
     * 基于RAG的智能问答
     */
    public RAGResponseVO ragQuery(RAGQueryDTO queryDTO, Integer userId) {
        try {
            long startTime = System.currentTimeMillis();
            
            // 验证知识库权限
            if (queryDTO.getKnowledgeBaseId() != null) {
                validateKnowledgeBaseAccess(queryDTO.getKnowledgeBaseId(), userId);
            }
            
            // 构建搜索请求
            SearchRequest.Builder searchBuilder = SearchRequest.builder()
                    .query(queryDTO.getQuery())
                    .topK(queryDTO.getTopK() != null ? queryDTO.getTopK() : 5)
                    .similarityThreshold(0.7);

            // 如果指定了知识库，添加过滤条件
            if (queryDTO.getKnowledgeBaseId() != null) {
                searchBuilder = searchBuilder.filterExpression("knowledge_base_id == '" + queryDTO.getKnowledgeBaseId() + "'");
            }

            SearchRequest searchRequest = searchBuilder.build();
            
            // 创建带RAG功能的ChatClient
            ChatClient chatClient = chatClientBuilder
                    .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                            .searchRequest(searchRequest)
                            .build()
                    )
                    .build();
            
            // 构建提示词
            String prompt = buildPrompt(queryDTO);
            
            // 执行查询
            String answer = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            // 获取参考来源
            List<Map<String, Object>> references = new ArrayList<>();
            if (queryDTO.getIncludeReferences() != null && queryDTO.getIncludeReferences()) {
                references = getReferences(queryDTO.getQuery(), searchRequest);
            }
            
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
            log.error("RAG查询失败", e);
            throw new RuntimeException("RAG查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 流式RAG问答
     */
    public Flux<String> ragQueryStream(RAGQueryDTO queryDTO, Integer userId) {
        try {
            // 验证知识库权限
            if (queryDTO.getKnowledgeBaseId() != null) {
                validateKnowledgeBaseAccess(queryDTO.getKnowledgeBaseId(), userId);
            }
            
            // 构建搜索请求
            SearchRequest.Builder searchBuilder = SearchRequest.builder()
                    .query(queryDTO.getQuery())
                    .topK(queryDTO.getTopK() != null ? queryDTO.getTopK() : 5)
                    .similarityThreshold(0.7);

            // 如果指定了知识库，添加过滤条件
            if (queryDTO.getKnowledgeBaseId() != null) {
                searchBuilder = searchBuilder.filterExpression("knowledge_base_id == '" + queryDTO.getKnowledgeBaseId() + "'");
            }

            SearchRequest searchRequest = searchBuilder.build();
            
            // 创建带RAG功能的ChatClient
            ChatClient chatClient = chatClientBuilder
                    .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                            .searchRequest(searchRequest)
                            .build()
                    )
                    .build();
            
            // 构建提示词
            String prompt = buildPrompt(queryDTO);
            
            // 执行流式查询
            return chatClient.prompt()
                    .user(prompt)
                    .stream()
                    .content();
                    
        } catch (Exception e) {
            log.error("流式RAG查询失败", e);
            return Flux.error(new RuntimeException("流式RAG查询失败: " + e.getMessage()));
        }
    }
    
    // ============ 私有方法 ============
    
    private void validateKnowledgeBaseAccess(String knowledgeBaseId, Integer userId) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            throw new RuntimeException("知识库不存在");
        }
        if (!knowledgeBase.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该知识库");
        }
        if (!"completed".equals(knowledgeBase.getStatus())) {
            throw new RuntimeException("知识库尚未构建完成");
        }
        
        // 更新最后使用时间
        knowledgeBase.setLastUsed(new Date());
        knowledgeBaseMapper.updateById(knowledgeBase);
    }
    
    private String buildPrompt(RAGQueryDTO queryDTO) {
        StringBuilder promptBuilder = new StringBuilder();
        
        // 根据回答模式调整提示词
        String answerMode = queryDTO.getAnswerMode() != null ? queryDTO.getAnswerMode() : "detailed";
        
        switch (answerMode) {
            case "concise":
                promptBuilder.append("请简洁地回答以下问题：\n");
                break;
            case "tutorial":
                promptBuilder.append("请以教学的方式详细解释以下问题，包含步骤和示例：\n");
                break;
            default:
                promptBuilder.append("请详细回答以下问题：\n");
                break;
        }
        
        promptBuilder.append(queryDTO.getQuery());
        
        return promptBuilder.toString();
    }
    
    private List<Map<String, Object>> getReferences(String query, SearchRequest searchRequest) {
        try {
            List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
            
            return similarDocuments.stream()
                    .limit(3) // 最多返回3个参考来源
                    .map(doc -> {
                        Map<String, Object> reference = new HashMap<>();
                        reference.put("resourceId", doc.getMetadata().get("resource_id"));
                        reference.put("title", doc.getMetadata().get("title"));
                        reference.put("relevanceScore", 0.96); // 这里应该是实际的相似度分数
                        reference.put("excerpt", doc.getText().substring(0, Math.min(200, doc.getText().length())) + "...");
                        return reference;
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.warn("获取参考来源失败", e);
            return new ArrayList<>();
        }
    }
    
    private List<String> generateRelatedQuestions(String query) {
        // 这里可以使用AI生成相关问题，暂时返回固定的相关问题
        return Arrays.asList(
            "能否提供更多相关的例子？",
            "这个概念在实际应用中如何使用？",
            "有哪些常见的误区需要注意？"
        );
    }
    
    private String generateMessageId() {
        return "msg_" + UUID.randomUUID().toString().replace("-", "");
    }
}
