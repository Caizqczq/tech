package com.mtm.backend.model.DTO;

import lombok.Data;

/**
 * RAG查询DTO
 */
@Data
public class RAGQueryDTO {
    
    /**
     * 用户问题
     */
    private String query;
    
    /**
     * 知识库ID
     */
    private String knowledgeBaseId;
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 回答模式
     */
    private String answerMode;
    
    /**
     * 是否包含参考来源
     */
    private Boolean includeReferences;
    
    /**
     * 检索文档数量
     */
    private Integer topK;
}
