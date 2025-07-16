package com.mtm.backend.model.DTO;

import lombok.Data;

@Data
public class PPTGenerationDTO {
    private String topic;
    private String subject;
    private String courseLevel;
    private Integer slideCount = 20;
    private String style;
    private Boolean includeFormulas = true;
    private Boolean includeProofs = false;
    private String targetAudience;
    private Integer duration = 45;
    private String language = "zh";
}