package com.mtm.backend.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import com.mtm.backend.model.DTO.TeachingAdviceDTO;
import com.mtm.backend.model.DTO.ContentAnalysisDTO;
import com.mtm.backend.model.DTO.WritingAssistanceDTO;
import com.mtm.backend.model.DTO.ChatAssistantDTO;
import com.mtm.backend.service.TeachingChatService;
import com.mtm.backend.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class TeachingChatController {
    
    private final TeachingChatService teachingChatService;
    private final ChatClient dashScopeChatClient;
    
    public TeachingChatController(TeachingChatService teachingChatService, ChatModel chatModel, JdbcTemplate jdbcTemplate) {
        this.teachingChatService = teachingChatService;
        
        // 配置专门用于教学场景的ChatClient
        var chatMemoryRepository = MysqlChatMemoryRepository.mysqlBuilder()
                .jdbcTemplate(jdbcTemplate)
                .build();
        var chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        
        this.dashScopeChatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一个专业的高等教育AI助手，专门帮助教师进行教学相关的工作。请始终以专业、准确、有建设性的方式回答问题。")
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
    
    /**
     * 教学建议API
     */
    @PostMapping("/teaching-advice")
    public ResponseEntity<?> getTeachingAdvice(@RequestBody TeachingAdviceDTO adviceDTO) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/chat/teaching-advice"));
            }
            
            // 验证必要参数
            if (adviceDTO.getQuery() == null || adviceDTO.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("问题内容不能为空", "/api/chat/teaching-advice"));
            }
            
            Object result = teachingChatService.getTeachingAdvice(adviceDTO, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取教学建议失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取教学建议失败: " + e.getMessage(), "/api/chat/teaching-advice"));
        }
    }
    
    /**
     * 课程内容分析API
     */
    @PostMapping("/content-analysis")
    public ResponseEntity<?> analyzeContent(@RequestBody ContentAnalysisDTO analysisDTO) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/chat/content-analysis"));
            }
            
            // 验证必要参数
            if (analysisDTO.getContent() == null || analysisDTO.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("分析内容不能为空", "/api/chat/content-analysis"));
            }
            
            if (analysisDTO.getAnalysisType() == null || analysisDTO.getAnalysisType().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("分析类型不能为空", "/api/chat/content-analysis"));
            }
            
            Object result = teachingChatService.analyzeContent(analysisDTO, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("内容分析失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("内容分析失败: " + e.getMessage(), "/api/chat/content-analysis"));
        }
    }
    
    /**
     * 学术写作辅助API
     */
    @PostMapping("/writing-assistance")
    public ResponseEntity<?> getWritingAssistance(@RequestBody WritingAssistanceDTO writingDTO) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/chat/writing-assistance"));
            }
            
            // 验证必要参数
            if (writingDTO.getContent() == null || writingDTO.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("写作内容不能为空", "/api/chat/writing-assistance"));
            }
            
            if (writingDTO.getAssistanceType() == null || writingDTO.getAssistanceType().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("辅助类型不能为空", "/api/chat/writing-assistance"));
            }
            
            Object result = teachingChatService.getWritingAssistance(writingDTO, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("写作辅助失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("写作辅助失败: " + e.getMessage(), "/api/chat/writing-assistance"));
        }
    }
    
    /**
     * 智能对话助手API
     */
    @PostMapping("/assistant")
    public ResponseEntity<?> chatWithAssistant(@RequestBody ChatAssistantDTO assistantDTO) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/chat/assistant"));
            }
            
            // 验证必要参数
            if (assistantDTO.getMessage() == null || assistantDTO.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("消息内容不能为空", "/api/chat/assistant"));
            }
            
            Object result = teachingChatService.chatWithAssistant(assistantDTO, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("对话助手失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("对话助手失败: " + e.getMessage(), "/api/chat/assistant"));
        }
    }
    
    /**
     * 流式智能对话助手API
     */
    @PostMapping("/assistant/stream")
    public Flux<String> streamChatWithAssistant(@RequestBody ChatAssistantDTO assistantDTO, 
                                               HttpServletResponse response) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return Flux.just("用户未登录");
            }
            
            // 验证必要参数
            if (assistantDTO.getMessage() == null || assistantDTO.getMessage().trim().isEmpty()) {
                return Flux.just("消息内容不能为空");
            }
            
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/event-stream");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            
            return teachingChatService.streamChatWithAssistant(assistantDTO, userId)
                    .doOnError(error -> log.error("流式对话失败", error))
                    .onErrorReturn("对话出现错误，请重试");
            
        } catch (Exception e) {
            log.error("流式对话助手初始化失败", e);
            return Flux.just("对话初始化失败，请重试");
        }
    }
    
    /**
     * 创建标准错误响应
     */
    private Map<String, Object> createErrorResponse(String message, String path) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", new Date().toString());
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message", message);
        error.put("path", path);
        return error;
    }
}