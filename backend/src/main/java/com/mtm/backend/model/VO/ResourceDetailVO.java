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
public class ResourceDetailVO {
    private String id;
    private String title;
    private String description;
    private String subject;
    private String courseLevel;
    private String resourceType;
    private String originalName;
    private Long fileSize;
    private String contentType;
    private List<String> keywords;
    private String downloadUrl;
    private String transcriptionText;
    private Boolean isVectorized;
    private List<String> knowledgeBaseIds;
    private Date createdAt;
    private Date updatedAt;
}