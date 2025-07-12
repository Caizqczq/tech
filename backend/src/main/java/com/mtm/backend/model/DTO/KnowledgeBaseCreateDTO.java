package com.mtm.backend.model.DTO;

import lombok.Data;

import java.util.List;

/**
 * 知识库创建DTO
 */
@Data
public class KnowledgeBaseCreateDTO {
    
    /**
     * 知识库名称
     */
    private String name;
    
    /**
     * 知识库描述
     */
    private String description;
    
    /**
     * 资源ID列表
     */
    private List<String> resourceIds;
    
    /**
     * 学科领域
     */
    private String subject;
    
    /**
     * 课程层次
     */
    private String courseLevel;
    
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
}
