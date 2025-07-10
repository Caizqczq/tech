package com.mtm.backend.model.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialUploadVO {
    private String id;
    private String filename;
    private String originalName;
    private String subject;
    private String courseLevel;
    private String documentType;
    private Long size;
    private String contentType;
    private List<String> keywords;
    private Date uploadedAt;
    private String downloadUrl;
    
    // 音频特有字段
    private String audioType;
    private String description;
    private String speaker;
    private Integer duration;
    private String language;
    private String transcription;
    private List<String> keyPoints;
}