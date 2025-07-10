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
    private String content;
    private String analysisType;
    private String subject;
    private String courseLevel;
    private String analysisScope;
    private String targetAudience;
}