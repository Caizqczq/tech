package com.mtm.backend.model.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAssistantDTO {
    private String message;
    private String conversationId;
    private String assistantMode;
    private String subject;
    private String courseLevel;
    private String streamMode;
    private String contextInfo;
}