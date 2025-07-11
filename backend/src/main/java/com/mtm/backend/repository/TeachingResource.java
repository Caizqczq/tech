package com.mtm.backend.repository;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("teaching_resources")
public class TeachingResource {
    
    @TableId(type = IdType.INPUT)
    private String id;
    
    private String originalName;
    private String storedFilename;
    private String ossKey;
    private String contentType;
    private Long fileSize;
    private String resourceType; // 改为resourceType以符合接口文档
    
    // 教学相关元数据
    private String title;
    private String description;
    private String subject;
    private String courseLevel;
    private String documentType;
    private String keywords;
    
    // 音频特有字段
    private Integer duration;
    private String language;
    private String audioType;
    private String speaker;
    private String transcriptionText;
    
    // 新增字段以支持接口文档要求
    private Boolean isVectorized; // 是否已向量化
    private String processingStatus; // 处理状态：processing/completed/failed
    private String extractedKeywords; // 自动提取的关键词
    
    // 所有者
    private Integer userId;
    
    private Date createdAt;
    private Date updatedAt;
}