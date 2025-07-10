package com.mtm.backend.model.VO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO {
    private String conversationId;
    private String title;
    private String scenario;
    private String contextInfo;
    private Integer totalMessages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}