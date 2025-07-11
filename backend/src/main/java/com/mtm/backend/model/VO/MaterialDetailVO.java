package com.mtm.backend.model.VO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDetailVO {
    private String materialId;
    private String originalName;
    private String materialType;
    private String contentType;
    private Long fileSize;
    private String downloadUrl;
    
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
    
    private Date createdAt;
    private Date updatedAt;
}