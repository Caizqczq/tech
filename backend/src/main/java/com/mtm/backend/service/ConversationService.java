package com.mtm.backend.service;

import com.mtm.backend.service.adapter.SpringAiChatMemoryAdapter;
import com.mtm.backend.repository.mapper.ConversationMapper;
import com.mtm.backend.repository.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 对话服务
 * 基于Spring AI ChatMemory的统一实现
 *
 * @author Claude Code
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final SpringAiChatMemoryAdapter chatMemoryAdapter;
    private final ConversationMapper conversationMapper;
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * 获取用户的对话列表
     */
    public Map<String, Object> getUserConversations(Integer userId, Integer page, Integer limit,
                                                   String scenario, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return chatMemoryAdapter.getUserConversations(userId, page, limit, scenario, startDate, endDate);
        } catch (Exception e) {
            log.error("获取对话列表失败", e);
            throw new RuntimeException("获取对话列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取对话详情
     */
    public Map<String, Object> getConversationDetail(String conversationId, Integer userId) {
        try {
            return chatMemoryAdapter.getConversationDetail(conversationId, userId);
        } catch (Exception e) {
            log.error("获取对话详情失败", e);
            throw new RuntimeException("获取对话详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除对话
     */
    public void deleteConversation(String conversationId, Integer userId) {
        try {
            chatMemoryAdapter.deleteConversation(conversationId, userId);
        } catch (Exception e) {
            log.error("删除对话失败", e);
            throw new RuntimeException("删除对话失败: " + e.getMessage());
        }
    }

    /**
     * 清空用户所有对话
     */
    public void clearAllConversations(Integer userId) {
        try {
            chatMemoryAdapter.clearAllConversations(userId);
        } catch (Exception e) {
            log.error("清空对话失败", e);
            throw new RuntimeException("清空对话失败: " + e.getMessage());
        }
    }

    /**
     * 更新对话标题
     */
    public void updateConversationTitle(String conversationId, String title, Integer userId) {
        chatMemoryAdapter.updateConversationTitle(conversationId, title, userId);
    }
    
    /**
     * 获取对话统计信息
     */
    public Map<String, Object> getConversationStats(Integer userId) {
        return chatMemoryAdapter.getConversationStatistics(userId);
    }
}
