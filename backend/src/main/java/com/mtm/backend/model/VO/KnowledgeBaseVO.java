package com.mtm.backend.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseVO {
    
    /**
     * 知识库ID
     */
    private String id;
    
    /**
     * 知识库ID（兼容字段）
     */
    private String knowledgeBaseId;
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 知识库名称
     */
    private String name;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 状态消息
     */
    private String message;
    
    /**
     * 预计时间
     */
    private Integer estimatedTime;
    
    /**
     * 资源数量
     */
    private Integer resourceCount;
    
    /**
     * 文档数量
     */
    private Integer documentCount;
    
    /**
     * 状态查询URL
     */
    private String statusUrl;
    
    /**
     * 创建时间
     */
    private String createdAt;
    
    /**
     * 更新时间
     */
    private String updatedAt;
}
