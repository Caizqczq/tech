package com.mtm.backend.repository;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 知识库实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_base")
public class KnowledgeBase {
    
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 知识库名称
     */
    private String name;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 学科
     */
    private String subject;
    
    /**
     * 课程层次
     */
    private String courseLevel;
    
    /**
     * 资源ID列表（JSON格式）
     */
    private String resourceIds;
    
    /**
     * 向量存储类型
     */
    private String vectorStore;
    
    /**
     * 分块大小
     */
    private Integer chunkSize;
    
    /**
     * 分块重叠
     */
    private Integer chunkOverlap;
    
    /**
     * 状态：processing, completed, failed
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
     * 文档数量（兼容字段）
     */
    private Integer documentCount;
    
    /**
     * 状态消息
     */
    private String message;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 更新时间
     */
    private Date updatedAt;
    
    /**
     * 完成时间
     */
    private Date completedAt;
    
    /**
     * 最后使用时间
     */
    private Date lastUsed;
}
