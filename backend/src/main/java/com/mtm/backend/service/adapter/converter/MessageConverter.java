package com.mtm.backend.service.adapter.converter;

import com.mtm.backend.model.VO.ChatMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring AI Message与业务VO之间的转换器
 *
 * @author Claude Code
 */
@Component
@Slf4j
public class MessageConverter {
    
    /**
     * 将Spring AI Message转换为ChatMessageVO
     */
    public ChatMessageVO convertToChatMessageVO(Message message) {
        return ChatMessageVO.builder()
                .messageId(generateMessageId())
                .role(convertMessageType(message.getMessageType())) // 改为role字段
                .content(message.getText())
                .metadata(extractMetadata(message))
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 转换消息类型
     */
    private String convertMessageType(MessageType messageType) {
        switch (messageType) {
            case USER:
                return "user";
            case ASSISTANT:
                return "assistant";
            case SYSTEM:
                return "system";
            default:
                return "unknown";
        }
    }
    
    /**
     * 提取消息元数据
     */
    private String extractMetadata(Message message) {
        try {
            if (message.getMetadata() != null && !message.getMetadata().isEmpty()) {
                // 简单的JSON格式化
                StringBuilder sb = new StringBuilder("{");
                message.getMetadata().forEach((key, value) -> {
                    sb.append("\"").append(key).append("\":\"").append(value).append("\",");
                });
                if (sb.length() > 1) {
                    sb.setLength(sb.length() - 1); // 移除最后的逗号
                }
                sb.append("}");
                return sb.toString();
            }
        } catch (Exception e) {
            log.warn("提取消息元数据失败: {}", e.getMessage());
        }
        return "{}";
    }
    
    /**
     * 生成消息ID
     */
    private String generateMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
