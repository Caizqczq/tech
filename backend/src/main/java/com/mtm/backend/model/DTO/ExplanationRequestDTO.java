package com.mtm.backend.model.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationRequestDTO {
    private String topic;
    private String subject;
    private String courseLevel;
    private String style;
    private String length;
    private Boolean includeExamples;
    private Boolean includeProofs;
    private Boolean includeApplications;
    private String targetAudience;
    private String language;
}