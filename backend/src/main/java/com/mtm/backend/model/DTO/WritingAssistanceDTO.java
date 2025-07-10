package com.mtm.backend.model.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingAssistanceDTO {
    private String content;
    private String writingType;
    private String subject;
    private String assistanceType;
    private String targetAudience;
    private String language;
    private String additionalRequirements;
}