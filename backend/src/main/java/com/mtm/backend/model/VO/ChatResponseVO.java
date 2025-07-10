package com.mtm.backend.model.VO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseVO {
    private String content;
    private String conversationId;
    private Map<String, Object> usage;
    private String responseTime;
    private String model;
    private String scenario;
}