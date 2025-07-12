package com.mtm.backend.model.VO;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 知识库创建响应VO
 */
@Data
@Builder
public class KnowledgeBaseVO {
    
    /**
     * 知识库ID
     */
    private String knowledgeBaseId;
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 预估时间（秒）
     */
    private Integer estimatedTime;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 资源数量
     */
    private Integer resourceCount;
    
    /**
     * 状态查询URL
     */
    private String statusUrl;
}
