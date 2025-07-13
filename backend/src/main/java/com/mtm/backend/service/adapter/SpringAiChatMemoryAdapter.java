package com.mtm.backend.service.adapter;

import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtm.backend.model.VO.ConversationVO;
import com.mtm.backend.model.VO.ChatMessageVO;
import com.mtm.backend.repository.Conversation;
import com.mtm.backend.repository.mapper.ConversationMapper;
import com.mtm.backend.service.adapter.converter.MessageConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring AI ChatMemory适配器
 * 提供基于Spring AI标准ChatMemory的对话管理功能
 * 
 * @author Claude Code
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpringAiChatMemoryAdapter {
    
    private final ConversationMapper conversationMapper;
    private final MysqlChatMemoryRepository chatMemoryRepository;
    private final MessageConverter messageConverter;
    
    /**
     * 获取用户的对话列表
     */
    public Map<String, Object> getUserConversations(Integer userId, Integer page, Integer limit, 
                                                   String scenario, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            // 构建查询条件
            QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            
            if (scenario != null && !scenario.trim().isEmpty()) {
                queryWrapper.eq("scenario", scenario);
            }
            
            if (startDate != null) {
                queryWrapper.ge("created_at", startDate);
            }
            
            if (endDate != null) {
                queryWrapper.le("created_at", endDate);
            }
            
            queryWrapper.orderByDesc("updated_at");
            
            // 分页查询
            Page<Conversation> pageInfo = new Page<>(page != null ? page : 1, limit != null ? limit : 10);
            IPage<Conversation> conversationPage = conversationMapper.selectPage(pageInfo, queryWrapper);
            
            // 转换为VO并获取最后一条消息
            List<ConversationVO> conversationVOs = conversationPage.getRecords().stream()
                    .map(this::convertToConversationVO)
                    .collect(Collectors.toList());
            
            return Map.of(
                "conversations", conversationVOs,
                "pagination", Map.of(
                    "currentPage", conversationPage.getCurrent(),
                    "pageSize", conversationPage.getSize(),
                    "total", conversationPage.getTotal(),
                    "totalPages", conversationPage.getPages()
                )
            );
            
        } catch (Exception e) {
            log.error("获取对话列表失败", e);
            throw new RuntimeException("获取对话列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取对话详情及消息历史
     */
    public Map<String, Object> getConversationDetail(String conversationId, Integer userId) {
        try {
            // 验证对话是否属于当前用户
            Conversation conversation = conversationMapper.selectById(conversationId);
            if (conversation == null) {
                throw new RuntimeException("对话不存在");
            }

            if (!conversation.getUserId().equals(userId)) {
                throw new RuntimeException("无权访问该对话");
            }

            // 通过ChatMemory获取消息
            ChatMemory chatMemory = MessageWindowChatMemory.builder()
                    .chatMemoryRepository(chatMemoryRepository)
                    .maxMessages(100) // 设置最大消息数量
                    .build();
            List<Message> messages = chatMemory.get(conversationId); // 获取消息

            // 转换为VO
            ConversationVO conversationVO = convertToConversationVO(conversation);
            List<ChatMessageVO> messageVOs = messages.stream()
                    .map(messageConverter::convertToChatMessageVO)
                    .collect(Collectors.toList());

            return Map.of(
                "conversation", conversationVO,
                "messages", messageVOs
            );

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
            // 验证对话是否属于当前用户
            Conversation conversation = conversationMapper.selectById(conversationId);
            if (conversation == null) {
                throw new RuntimeException("对话不存在");
            }

            if (!conversation.getUserId().equals(userId)) {
                throw new RuntimeException("无权删除该对话");
            }

            // 通过ChatMemory清空对话消息
            ChatMemory chatMemory = MessageWindowChatMemory.builder()
                    .chatMemoryRepository(chatMemoryRepository)
                    .build();
            chatMemory.clear(conversationId);

            // 删除对话记录
            conversationMapper.deleteById(conversationId);

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
            // 获取用户所有对话ID
            QueryWrapper<Conversation> conversationQuery = new QueryWrapper<>();
            conversationQuery.eq("user_id", userId);
            List<Conversation> conversations = conversationMapper.selectList(conversationQuery);

            List<String> conversationIds = conversations.stream()
                    .map(Conversation::getId)
                    .collect(Collectors.toList());

            if (!conversationIds.isEmpty()) {
                // 通过ChatMemory清空所有对话消息
                ChatMemory chatMemory = MessageWindowChatMemory.builder()
                        .chatMemoryRepository(chatMemoryRepository)
                        .build();
                for (String conversationId : conversationIds) {
                    chatMemory.clear(conversationId);
                }

                // 删除所有对话记录
                conversationMapper.delete(conversationQuery);
            }

        } catch (Exception e) {
            log.error("清空对话失败", e);
            throw new RuntimeException("清空对话失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新对话标题
     */
    public void updateConversationTitle(String conversationId, Integer userId, String newTitle) {
        try {
            // 验证对话是否属于当前用户
            Conversation conversation = conversationMapper.selectById(conversationId);
            if (conversation == null) {
                throw new RuntimeException("对话不存在");
            }
            
            if (!conversation.getUserId().equals(userId)) {
                throw new RuntimeException("无权修改该对话");
            }
            
            // 更新标题
            conversation.setTitle(newTitle);
            conversationMapper.updateById(conversation);
            
        } catch (Exception e) {
            log.error("更新对话标题失败", e);
            throw new RuntimeException("更新对话标题失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取对话统计信息
     */
    public Map<String, Object> getConversationStatistics(Integer userId) {
        try {
            // 总对话数
            QueryWrapper<Conversation> totalQuery = new QueryWrapper<>();
            totalQuery.eq("user_id", userId);
            Long totalConversations = conversationMapper.selectCount(totalQuery);
            
            // 按场景分组统计
            Map<String, Long> scenarioStats = Map.of(
                "teaching_advice", getConversationCountByScenario(userId, "teaching_advice"),
                "content_analysis", getConversationCountByScenario(userId, "content_analysis"),
                "writing_assistance", getConversationCountByScenario(userId, "writing_assistance"),
                "general_chat", getConversationCountByScenario(userId, "general_chat")
            );
            
            // 今日对话数
            QueryWrapper<Conversation> todayQuery = new QueryWrapper<>();
            todayQuery.eq("user_id", userId);
            todayQuery.ge("created_at", LocalDateTime.now().toLocalDate().atStartOfDay());
            Long todayConversations = conversationMapper.selectCount(todayQuery);
            
            return Map.of(
                "totalConversations", totalConversations,
                "scenarioStatistics", scenarioStats,
                "todayConversations", todayConversations
            );
            
        } catch (Exception e) {
            log.error("获取对话统计失败", e);
            throw new RuntimeException("获取对话统计失败: " + e.getMessage());
        }
    }
    
    // ============ 私有辅助方法 ============
    
    private ConversationVO convertToConversationVO(Conversation conversation) {
        // 获取最后一条消息
        String lastMessage = getLastMessage(conversation.getId());
        
        return ConversationVO.builder()
                .conversationId(conversation.getId())
                .title(conversation.getTitle())
                .scenario(conversation.getScenario())
                .contextInfo(conversation.getContextInfo())
                .totalMessages(conversation.getTotalMessages())
                .lastMessage(lastMessage)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
    
    private String getLastMessage(String conversationId) {
        try {
            ChatMemory chatMemory = MessageWindowChatMemory.builder()
                    .chatMemoryRepository(chatMemoryRepository)
                    .maxMessages(1) // 只保留最后一条消息
                    .build();
            List<Message> messages = chatMemory.get(conversationId); // 获取消息
            if (!messages.isEmpty()) {
                Message lastMessage = messages.get(messages.size() - 1);
                return lastMessage.getText(); // 使用getText()方法
            }
            return null;
        } catch (Exception e) {
            log.warn("获取最后一条消息失败: {}", e.getMessage());
            return null;
        }
    }
    
    private Long getConversationCountByScenario(Integer userId, String scenario) {
        QueryWrapper<Conversation> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.eq("scenario", scenario);
        return conversationMapper.selectCount(query);
    }
}
