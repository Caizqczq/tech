package com.mtm.backend.model.VO;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 知识库列表VO
 */
@Data
@Builder
public class KnowledgeBaseListVO {
    
    /**
     * 知识库ID
     */
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
     * 学科
     */
    private String subject;
    
    /**
     * 课程层次
     */
    private String courseLevel;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 资源数量
     */
    private Integer resourceCount;
    
    /**
     * 分块数量
     */
    private Integer chunkCount;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 最后使用时间
     */
    private Date lastUsed;
}
