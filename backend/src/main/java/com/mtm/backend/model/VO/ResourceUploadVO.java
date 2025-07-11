package com.mtm.backend.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceUploadVO {
    private String id;
    private String filename;
    private String originalName;
    private String subject;
    private String courseLevel;
    private String resourceType;
    private Long size;
    private String contentType;
    private List<String> keywords;
    private List<String> extractedKeywords;
    private Date uploadedAt;
    private String downloadUrl;
    private Boolean isVectorized;
    private String processingStatus;
    
    // 音频特有字段
    private String description;
    private String speaker;
    private Integer duration;
    private String language;
    private String transcription;
}