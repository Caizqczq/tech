package com.mtm.backend.model.VO;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 知识库状态VO
 */
@Data
@Builder
public class KnowledgeBaseStatusVO {
    
    /**
     * 知识库ID
     */
    private String knowledgeBaseId;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 进度
     */
    private Integer progress;
    
    /**
     * 资源数量
     */
    private Integer resourceCount;
    
    /**
     * 分块数量
     */
    private Integer chunkCount;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 完成时间
     */
    private Date completedAt;
}
