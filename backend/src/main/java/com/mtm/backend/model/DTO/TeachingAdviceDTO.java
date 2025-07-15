package com.mtm.backend.model.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingAdviceDTO {
    // 前端必需字段
    private String subject;
    private String grade;
    private String topic;
    private String difficulty;
    private String requirements;
    
    // 原有字段，保持向后兼容
    private String query;
    private String courseLevel;
    private String teachingType;
    private String currentContext;
    private String mode;
}
