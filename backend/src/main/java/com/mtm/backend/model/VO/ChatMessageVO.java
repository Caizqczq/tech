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
    private String role; // 改为role，与前端保持一致
    private String content;
    private String metadata;
    private LocalDateTime createdAt;
}
