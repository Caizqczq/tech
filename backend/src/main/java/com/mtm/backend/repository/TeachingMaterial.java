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
@TableName("teaching_materials")
public class TeachingMaterial {
    
    @TableId(type = IdType.INPUT)
    private String id;
    
    private String originalName;
    private String storedFilename;
    private String ossKey;
    private String contentType;
    private Long fileSize;
    private String materialType;
    
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
    
    // 所有者
    private Integer userId;
    
    private Date createdAt;
    private Date updatedAt;
}