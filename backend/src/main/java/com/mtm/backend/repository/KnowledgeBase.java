package com.mtm.backend.repository;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 知识库实体类
 */
@Data
@TableName("knowledge_base")
public class KnowledgeBase {
    
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 知识库名称
     */
    private String name;
    
    /**
     * 知识库描述
     */
    private String description;
    
    /**
     * 学科领域
     */
    private String subject;
    
    /**
     * 课程层次
     */
    private String courseLevel;
    
    /**
     * 资源ID列表（JSON格式存储）
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
     * 分块数量
     */
    private Integer chunkCount;
    
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
