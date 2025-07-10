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
public class ChatMessageVO {
    private String messageId;
    private String messageType;
    private String content;
    private String metadata;
    private LocalDateTime createdAt;
}