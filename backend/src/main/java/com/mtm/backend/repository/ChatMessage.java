package com.mtm.backend.repository;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_messages")
public class ChatMessage {
    
    @TableId
    private String id;
    
    private String conversationId;
    
    private String messageType;
    
    private String content;
    
    private String metadata;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}