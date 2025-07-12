package com.mtm.backend.model.VO;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * RAG问答响应VO
 */
@Data
@Builder
public class RAGResponseVO {
    
    /**
     * 回答内容
     */
    private String answer;
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 参考来源
     */
    private List<Map<String, Object>> references;
    
    /**
     * 相关问题
     */
    private List<String> relatedQuestions;
    
    /**
     * 处理时间（秒）
     */
    private Double processingTime;
    
    /**
     * 时间戳
     */
    private Date timestamp;
}
