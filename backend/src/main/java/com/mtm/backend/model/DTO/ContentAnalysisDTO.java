package com.mtm.backend.model.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentAnalysisDTO {
    // 前端必需字段
    private String content;
    private String analysisType;
    private String requirements;
    
    // 原有字段，保持向后兼容
    private String subject;
    private String courseLevel;
    private String analysisScope;
    private String targetAudience;
}
