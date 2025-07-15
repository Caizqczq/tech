package com.mtm.backend.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 知识库列表视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * 状态
     */
    private String status;
    
    /**
     * 资源数量
     */
    private Integer resourceCount;
    
    /**
     * 文档块数量
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
