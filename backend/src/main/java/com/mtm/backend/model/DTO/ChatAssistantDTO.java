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
    private String mode;           // 改为mode
    private String assistantMode;  // 保留兼容性
    private String subject;
    private String grade;          // 添加grade字段
    private String courseLevel;    // 保留兼容性
    private String topic;          // 添加topic字段
    private String streamMode;
    private String contextInfo;
    
    // 添加context对象支持
    private Context context;
    
    @Data
    public static class Context {
        private String subject;
        private String grade;
        private String topic;
    }
}
