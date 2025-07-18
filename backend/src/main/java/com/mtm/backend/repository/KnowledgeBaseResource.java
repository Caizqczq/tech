package com.mtm.backend.repository;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 知识库资源关联表
 */
@Data
@TableName("knowledge_base_resources")
public class KnowledgeBaseResource {
    
    @TableId
    private String id;
    
    private String knowledgeBaseId;
    
    private String resourceId;
    
    private Date addedAt;
}