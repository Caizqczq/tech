package com.mtm.backend.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 知识库状态视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * 进度百分比
     */
    private Integer progress;
    
    /**
     * 资源数量
     */
    private Integer resourceCount;
    
    /**
     * 文档块数量
     */
    private Integer chunkCount;
    
    /**
     * 状态消息
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
