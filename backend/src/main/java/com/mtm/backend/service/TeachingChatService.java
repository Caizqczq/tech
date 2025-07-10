package com.mtm.backend.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtm.backend.model.DTO.TeachingAdviceDTO;
import com.mtm.backend.model.DTO.ContentAnalysisDTO;
import com.mtm.backend.model.DTO.WritingAssistanceDTO;
import com.mtm.backend.model.DTO.ChatAssistantDTO;
import com.mtm.backend.model.VO.ChatResponseVO;
import com.mtm.backend.repository.Conversation;
import com.mtm.backend.repository.ChatMessage;
import com.mtm.backend.repository.mapper.ConversationMapper;
import com.mtm.backend.repository.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeachingChatService {
    
    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatModel chatModel;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 获取教学建议
     */
    public ChatResponseVO getTeachingAdvice(TeachingAdviceDTO adviceDTO, Integer userId) {
        try {
            // 构建教学场景的系统提示词
            String systemPrompt = buildTeachingAdviceSystemPrompt(adviceDTO);
            
            // 构建用户查询
            String userQuery = buildTeachingAdviceQuery(adviceDTO);
            
            // 创建对话ID
            String conversationId = adviceDTO.getMode() != null && "new".equals(adviceDTO.getMode()) 
                ? UUID.randomUUID().toString().replace("-", "")
                : "teaching_advice_" + userId;
            
            // 创建ChatClient
            ChatClient chatClient = createTeachingChatClient(systemPrompt);
            
            // 发送请求
            ChatResponse response = chatClient.prompt(userQuery)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call();
            
            // 保存对话记录
            saveConversation(conversationId, userId, "教学建议对话", "teaching_advice", adviceDTO);
            saveChatMessage(conversationId, "user", userQuery, null);
            saveChatMessage(conversationId, "assistant", response.getResult().getOutput().getText(), 
                    Map.of("usage", response.getMetadata().getUsage()));
            
            // 构建响应
            return ChatResponseVO.builder()
                    .content(response.getResult().getOutput().getText())
                    .conversationId(conversationId)
                    .usage(Map.of(
                        "promptTokens", response.getMetadata().getUsage().getPromptTokens(),
                        "completionTokens", response.getMetadata().getUsage().getCompletionTokens(),
                        "totalTokens", response.getMetadata().getUsage().getTotalTokens()
                    ))
                    .responseTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .model("qwen-plus")
                    .scenario("teaching_advice")
                    .build();
                    
        } catch (Exception e) {
            log.error("获取教学建议失败", e);
            throw new RuntimeException("获取教学建议失败: " + e.getMessage());
        }
    }
    
    /**
     * 分析课程内容
     */
    public ChatResponseVO analyzeContent(ContentAnalysisDTO analysisDTO, Integer userId) {
        try {
            // 构建内容分析的系统提示词
            String systemPrompt = buildContentAnalysisSystemPrompt(analysisDTO);
            
            // 构建分析查询
            String analysisQuery = buildContentAnalysisQuery(analysisDTO);
            
            // 创建对话ID
            String conversationId = "content_analysis_" + UUID.randomUUID().toString().replace("-", "");
            
            // 创建ChatClient
            ChatClient chatClient = createTeachingChatClient(systemPrompt);
            
            // 发送请求
            ChatResponse response = chatClient.prompt(analysisQuery)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call();
            
            // 保存对话记录
            saveConversation(conversationId, userId, "课程内容分析", "content_analysis", analysisDTO);
            saveChatMessage(conversationId, "user", analysisQuery, null);
            saveChatMessage(conversationId, "assistant", response.getResult().getOutput().getText(), 
                    Map.of("usage", response.getMetadata().getUsage()));
            
            // 构建响应
            return ChatResponseVO.builder()
                    .content(response.getResult().getOutput().getText())
                    .conversationId(conversationId)
                    .usage(Map.of(
                        "promptTokens", response.getMetadata().getUsage().getPromptTokens(),
                        "completionTokens", response.getMetadata().getUsage().getCompletionTokens(),
                        "totalTokens", response.getMetadata().getUsage().getTotalTokens()
                    ))
                    .responseTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .model("qwen-plus")
                    .scenario("content_analysis")
                    .build();
                    
        } catch (Exception e) {
            log.error("内容分析失败", e);
            throw new RuntimeException("内容分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 学术写作辅助
     */
    public ChatResponseVO getWritingAssistance(WritingAssistanceDTO writingDTO, Integer userId) {
        try {
            // 构建写作辅助的系统提示词
            String systemPrompt = buildWritingAssistanceSystemPrompt(writingDTO);
            
            // 构建写作查询
            String writingQuery = buildWritingAssistanceQuery(writingDTO);
            
            // 创建对话ID
            String conversationId = "writing_assistance_" + UUID.randomUUID().toString().replace("-", "");
            
            // 创建ChatClient
            ChatClient chatClient = createTeachingChatClient(systemPrompt);
            
            // 发送请求
            ChatResponse response = chatClient.prompt(writingQuery)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call();
            
            // 保存对话记录
            saveConversation(conversationId, userId, "学术写作辅助", "writing_assistance", writingDTO);
            saveChatMessage(conversationId, "user", writingQuery, null);
            saveChatMessage(conversationId, "assistant", response.getResult().getOutput().getText(), 
                    Map.of("usage", response.getMetadata().getUsage()));
            
            // 构建响应
            return ChatResponseVO.builder()
                    .content(response.getResult().getOutput().getText())
                    .conversationId(conversationId)
                    .usage(Map.of(
                        "promptTokens", response.getMetadata().getUsage().getPromptTokens(),
                        "completionTokens", response.getMetadata().getUsage().getCompletionTokens(),
                        "totalTokens", response.getMetadata().getUsage().getTotalTokens()
                    ))
                    .responseTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .model("qwen-plus")
                    .scenario("writing_assistance")
                    .build();
                    
        } catch (Exception e) {
            log.error("写作辅助失败", e);
            throw new RuntimeException("写作辅助失败: " + e.getMessage());
        }
    }
    
    /**
     * 智能对话助手
     */
    public ChatResponseVO chatWithAssistant(ChatAssistantDTO assistantDTO, Integer userId) {
        try {
            // 构建助手的系统提示词
            String systemPrompt = buildAssistantSystemPrompt(assistantDTO);
            
            // 获取或创建对话ID
            String conversationId = assistantDTO.getConversationId() != null 
                ? assistantDTO.getConversationId()
                : "assistant_" + UUID.randomUUID().toString().replace("-", "");
            
            // 创建ChatClient
            ChatClient chatClient = createTeachingChatClient(systemPrompt);
            
            // 发送请求
            ChatResponse response = chatClient.prompt(assistantDTO.getMessage())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call();
            
            // 保存对话记录
            if (assistantDTO.getConversationId() == null) {
                saveConversation(conversationId, userId, "智能对话助手", "general_chat", assistantDTO);
            }
            saveChatMessage(conversationId, "user", assistantDTO.getMessage(), null);
            saveChatMessage(conversationId, "assistant", response.getResult().getOutput().getText(), 
                    Map.of("usage", response.getMetadata().getUsage()));
            
            // 构建响应
            return ChatResponseVO.builder()
                    .content(response.getResult().getOutput().getText())
                    .conversationId(conversationId)
                    .usage(Map.of(
                        "promptTokens", response.getMetadata().getUsage().getPromptTokens(),
                        "completionTokens", response.getMetadata().getUsage().getCompletionTokens(),
                        "totalTokens", response.getMetadata().getUsage().getTotalTokens()
                    ))
                    .responseTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .model("qwen-plus")
                    .scenario("general_chat")
                    .build();
                    
        } catch (Exception e) {
            log.error("对话助手失败", e);
            throw new RuntimeException("对话助手失败: " + e.getMessage());
        }
    }
    
    /**
     * 流式智能对话助手
     */
    public Flux<String> streamChatWithAssistant(ChatAssistantDTO assistantDTO, Integer userId) {
        try {
            // 构建助手的系统提示词
            String systemPrompt = buildAssistantSystemPrompt(assistantDTO);
            
            // 获取或创建对话ID
            String conversationId = assistantDTO.getConversationId() != null 
                ? assistantDTO.getConversationId()
                : "assistant_stream_" + UUID.randomUUID().toString().replace("-", "");
            
            // 创建ChatClient
            ChatClient chatClient = createTeachingChatClient(systemPrompt);
            
            // 异步保存用户消息
            if (assistantDTO.getConversationId() == null) {
                saveConversation(conversationId, userId, "智能对话助手(流式)", "general_chat", assistantDTO);
            }
            saveChatMessage(conversationId, "user", assistantDTO.getMessage(), null);
            
            // 返回流式响应
            return chatClient.prompt(assistantDTO.getMessage())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .stream()
                    .content()
                    .doOnComplete(() -> {
                        // 流式完成后保存助手消息
                        saveChatMessage(conversationId, "assistant", "流式响应完成", null);
                    });
                    
        } catch (Exception e) {
            log.error("流式对话助手失败", e);
            return Flux.just("对话出现错误: " + e.getMessage());
        }
    }
    
    // ============ 私有辅助方法 ============
    
    private ChatClient createTeachingChatClient(String systemPrompt) {
        var chatMemoryRepository = MysqlChatMemoryRepository.mysqlBuilder()
                .jdbcTemplate(jdbcTemplate)
                .build();
        var chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        
        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .withTemperature(0.7)
                                .build()
                )
                .build();
    }
    
    private String buildTeachingAdviceSystemPrompt(TeachingAdviceDTO adviceDTO) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的高等教育教学顾问AI助手。你的职责是为教师提供专业、实用的教学建议。");
        
        if (adviceDTO.getSubject() != null) {
            prompt.append("当前学科领域：").append(adviceDTO.getSubject()).append("。");
        }
        if (adviceDTO.getCourseLevel() != null) {
            prompt.append("课程层次：").append(adviceDTO.getCourseLevel()).append("。");
        }
        if (adviceDTO.getTeachingType() != null) {
            prompt.append("教学类型：").append(adviceDTO.getTeachingType()).append("。");
        }
        
        prompt.append("请基于教学理论和实践经验，提供具体可行的建议。");
        return prompt.toString();
    }
    
    private String buildTeachingAdviceQuery(TeachingAdviceDTO adviceDTO) {
        StringBuilder query = new StringBuilder();
        query.append("教学问题：").append(adviceDTO.getQuery());
        
        if (adviceDTO.getCurrentContext() != null) {
            query.append("\n当前教学背景：").append(adviceDTO.getCurrentContext());
        }
        
        return query.toString();
    }
    
    private String buildContentAnalysisSystemPrompt(ContentAnalysisDTO analysisDTO) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的教育内容分析专家。你的任务是深入分析教学内容，提供专业的分析报告。");
        
        if (analysisDTO.getSubject() != null) {
            prompt.append("分析学科：").append(analysisDTO.getSubject()).append("。");
        }
        if (analysisDTO.getCourseLevel() != null) {
            prompt.append("课程层次：").append(analysisDTO.getCourseLevel()).append("。");
        }
        if (analysisDTO.getTargetAudience() != null) {
            prompt.append("目标受众：").append(analysisDTO.getTargetAudience()).append("。");
        }
        
        prompt.append("请从教学目标、内容结构、难度层次、教学方法等维度进行分析。");
        return prompt.toString();
    }
    
    private String buildContentAnalysisQuery(ContentAnalysisDTO analysisDTO) {
        StringBuilder query = new StringBuilder();
        query.append("分析类型：").append(analysisDTO.getAnalysisType());
        query.append("\n需要分析的内容：\n").append(analysisDTO.getContent());
        
        if (analysisDTO.getAnalysisScope() != null) {
            query.append("\n分析范围：").append(analysisDTO.getAnalysisScope());
        }
        
        return query.toString();
    }
    
    private String buildWritingAssistanceSystemPrompt(WritingAssistanceDTO writingDTO) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的学术写作助手。你的任务是帮助用户改进和完善学术写作。");
        
        if (writingDTO.getSubject() != null) {
            prompt.append("写作学科领域：").append(writingDTO.getSubject()).append("。");
        }
        if (writingDTO.getTargetAudience() != null) {
            prompt.append("目标读者：").append(writingDTO.getTargetAudience()).append("。");
        }
        if (writingDTO.getLanguage() != null) {
            prompt.append("写作语言：").append(writingDTO.getLanguage()).append("。");
        }
        
        prompt.append("请提供专业、准确的写作建议，注重学术规范性和表达清晰性。");
        return prompt.toString();
    }
    
    private String buildWritingAssistanceQuery(WritingAssistanceDTO writingDTO) {
        StringBuilder query = new StringBuilder();
        query.append("写作类型：").append(writingDTO.getWritingType());
        query.append("\n辅助类型：").append(writingDTO.getAssistanceType());
        query.append("\n写作内容：\n").append(writingDTO.getContent());
        
        if (writingDTO.getAdditionalRequirements() != null) {
            query.append("\n额外要求：").append(writingDTO.getAdditionalRequirements());
        }
        
        return query.toString();
    }
    
    private String buildAssistantSystemPrompt(ChatAssistantDTO assistantDTO) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个智能教学助手，专门为高等教育教师提供全方位的支持和帮助。");
        
        if (assistantDTO.getAssistantMode() != null) {
            switch (assistantDTO.getAssistantMode()) {
                case "teaching":
                    prompt.append("当前模式：教学模式。专注于教学相关的问题和建议。");
                    break;
                case "research":
                    prompt.append("当前模式：研究模式。专注于学术研究和论文写作。");
                    break;
                default:
                    prompt.append("当前模式：通用模式。可以处理各种教育相关问题。");
            }
        }
        
        if (assistantDTO.getSubject() != null) {
            prompt.append("专业领域：").append(assistantDTO.getSubject()).append("。");
        }
        if (assistantDTO.getCourseLevel() != null) {
            prompt.append("教学层次：").append(assistantDTO.getCourseLevel()).append("。");
        }
        
        prompt.append("请以专业、友好、有帮助的方式回答问题。");
        return prompt.toString();
    }
    
    private void saveConversation(String conversationId, Integer userId, String title, String scenario, Object contextObj) {
        try {
            String contextInfo = objectMapper.writeValueAsString(contextObj);
            
            Conversation conversation = Conversation.builder()
                    .id(conversationId)
                    .userId(userId)
                    .title(title)
                    .scenario(scenario)
                    .contextInfo(contextInfo)
                    .totalMessages(0)
                    .build();
            
            conversationMapper.insert(conversation);
        } catch (Exception e) {
            log.error("保存对话失败", e);
        }
    }
    
    private void saveChatMessage(String conversationId, String messageType, String content, Object metadata) {
        try {
            String metadataJson = metadata != null ? objectMapper.writeValueAsString(metadata) : null;
            
            ChatMessage message = ChatMessage.builder()
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .conversationId(conversationId)
                    .messageType(messageType)
                    .content(content)
                    .metadata(metadataJson)
                    .build();
            
            chatMessageMapper.insert(message);
            
            // 更新对话消息计数
            updateConversationMessageCount(conversationId);
        } catch (Exception e) {
            log.error("保存消息失败", e);
        }
    }
    
    private void updateConversationMessageCount(String conversationId) {
        try {
            Conversation conversation = conversationMapper.selectById(conversationId);
            if (conversation != null) {
                conversation.setTotalMessages(conversation.getTotalMessages() + 1);
                conversationMapper.updateById(conversation);
            }
        } catch (Exception e) {
            log.error("更新对话消息计数失败", e);
        }
    }
}