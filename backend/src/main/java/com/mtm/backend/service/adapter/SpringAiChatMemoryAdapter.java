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
            log.info("开始查询用户对话列表: userId={}, page={}, limit={}, scenario={}", userId, page, limit, scenario);
            
            // 构建查询条件
            QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            
            if (scenario != null && !scenario.trim().isEmpty()) {
                queryWrapper.eq("scenario", scenario);
                log.info("添加场景过滤条件: scenario={}", scenario);
            }
            
            if (startDate != null) {
                queryWrapper.ge("created_at", startDate);
                log.info("添加开始时间过滤条件: startDate={}", startDate);
            }
            
            if (endDate != null) {
                queryWrapper.le("created_at", endDate);
                log.info("添加结束时间过滤条件: endDate={}", endDate);
            }
            
            queryWrapper.orderByDesc("updated_at");
            
            // 先测试不分页的查询
            List<Conversation> allConversations = conversationMapper.selectList(queryWrapper);
            log.info("不分页查询结果: 总数={}", allConversations.size());
            for (Conversation conv : allConversations) {
                log.info("对话记录: id={}, title={}, userId={}, scenario={}, createdAt={}", 
                        conv.getId(), conv.getTitle(), conv.getUserId(), conv.getScenario(), conv.getCreatedAt());
            }
            
            // 分页查询
            Page<Conversation> pageInfo = new Page<>(page != null ? page : 1, limit != null ? limit : 10);
            log.info("分页参数: current={}, size={}", pageInfo.getCurrent(), pageInfo.getSize());
            
            IPage<Conversation> conversationPage = conversationMapper.selectPage(pageInfo, queryWrapper);
            
            log.info("分页查询结果: total={}, records={}, current={}, size={}", 
                    conversationPage.getTotal(), 
                    conversationPage.getRecords().size(),
                    conversationPage.getCurrent(),
                    conversationPage.getSize());
            
            // 转换为VO并获取最后一条消息
            List<ConversationVO> conversationVOs = conversationPage.getRecords().stream()
                    .map(conversation -> {
                        log.debug("处理对话: id={}, title={}", conversation.getId(), conversation.getTitle());
                        return convertToConversationVO(conversation);
                    })
                    .collect(Collectors.toList());
            
            log.info("转换后的VO数量: {}", conversationVOs.size());
            
            Map<String, Object> result = Map.of(
                "conversations", conversationVOs,
                "pagination", Map.of(
                    "currentPage", conversationPage.getCurrent(),
                    "pageSize", conversationPage.getSize(),
                    "total", conversationPage.getTotal(),
                    "totalPages", conversationPage.getPages()
                )
            );
            
            log.info("返回结果: {}", result);
            return result;
            
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
    public void updateConversationTitle(String conversationId, String title, Integer userId) {
        try {
            QueryWrapper<Conversation> query = new QueryWrapper<>();
            query.eq("conversation_id", conversationId);
            query.eq("user_id", userId);
            
            Conversation conversation = conversationMapper.selectOne(query);
            if (conversation == null) {
                throw new RuntimeException("对话不存在或无权访问");
            }
            
            conversation.setTitle(title);
            conversation.setUpdatedAt(LocalDateTime.now());
            conversationMapper.updateById(conversation);
            
            log.info("更新对话标题成功，对话ID：{}，新标题：{}", conversationId, title);
            
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
    
    public ConversationVO convertToConversationVO(Conversation conversation) {
        try {
            log.debug("转换对话VO: conversationId={}", conversation.getId());
            
            // 获取最后一条消息
            String lastMessage = getLastMessageContent(conversation.getId());
            log.debug("最后一条消息: {}", lastMessage);
            
            ConversationVO vo = ConversationVO.builder()
                    .conversationId(conversation.getId())
                    .title(conversation.getTitle())
                    .scenario(conversation.getScenario())
                    .contextInfo(conversation.getContextInfo())
                    .totalMessages(conversation.getTotalMessages())
                    .lastMessage(lastMessage)
                    .createdAt(conversation.getCreatedAt())
                    .updatedAt(conversation.getUpdatedAt())
                    .build();
                    
            log.debug("转换完成的VO: {}", vo);
            return vo;
            
        } catch (Exception e) {
            log.error("转换ConversationVO失败: conversationId={}", conversation.getId(), e);
            // 返回基本信息，避免整个列表失败
            return ConversationVO.builder()
                    .conversationId(conversation.getId())
                    .title(conversation.getTitle())
                    .scenario(conversation.getScenario())
                    .contextInfo(conversation.getContextInfo())
                    .totalMessages(conversation.getTotalMessages())
                    .lastMessage("获取消息失败")
                    .createdAt(conversation.getCreatedAt())
                    .updatedAt(conversation.getUpdatedAt())
                    .build();
        }
    }
    
    private String getLastMessageContent(String conversationId) {
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
