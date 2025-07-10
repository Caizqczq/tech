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
    private String query;
    private String subject;
    private String courseLevel;
    private String teachingType;
    private String currentContext;
    private String mode;
}