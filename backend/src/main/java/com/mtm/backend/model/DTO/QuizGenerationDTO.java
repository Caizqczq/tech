package com.mtm.backend.model.DTO;

import lombok.Data;

@Data
public class QuizGenerationDTO {
    private String topic;
    private String subject;
    private String courseLevel;
    private String difficulty = "medium";
    private Integer questionCount = 10;
    private String questionTypes;
    private Boolean includeSteps = true;
    private Boolean includeAnswers = true;
    private Integer timeLimit = 60;
    private String language = "zh";
}