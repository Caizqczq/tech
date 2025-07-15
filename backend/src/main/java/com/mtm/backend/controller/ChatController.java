package com.mtm.backend.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtm.backend.repository.Conversation;
import com.mtm.backend.repository.mapper.ConversationMapper;
import com.mtm.backend.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api")
@Slf4j
public class ChatController {

    private static final String DEFAULT_PROMPT = "不要返回markdown";

    private final ChatClient dashScopeChatClient;
    private final ConversationMapper conversationMapper;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ChatController(ChatModel chatModel, JdbcTemplate jdbcTemplate, ConversationMapper conversationMapper) {
        ChatMemoryRepository chatMemoryRepository = MysqlChatMemoryRepository.mysqlBuilder()
                .jdbcTemplate(jdbcTemplate)
                .build();
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .build();

        this.dashScopeChatClient = ChatClient.builder(chatModel)
                .defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .build()
                )
                .build();
        this.conversationMapper = conversationMapper;
        this.jdbcTemplate = jdbcTemplate;
    }


    /** 2.1 简单对话接口 */
    @GetMapping("/simple/chat")
    public ResponseEntity<?> simpleChat(@RequestParam(value = "query",defaultValue = "你好,能简单介绍一下自己吗")String query,
                                       @RequestParam(value = "chat-id",defaultValue = "1")String chatId) {
        try {
            log.info("simple chat query:{},chat-id:{}", query, chatId);
            
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/simple/chat"));
            }
            
            ChatResponse response = dashScopeChatClient.prompt(query)
                    .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatId))
                    .call().chatResponse();
            
            // 保存对话记录到conversations表
            String conversationId = chatId;
            try {
                // 如果是默认chatId，生成新的conversationId
                if (chatId.equals("1")) {
                    conversationId = "simple_chat_" + UUID.randomUUID().toString().replace("-", "");
                }
                
                log.info("准备保存对话记录: conversationId={}, userId={}", conversationId, userId);
                
                // 构建简单的上下文信息
                Map<String, Object> contextObj = Map.of(
                    "mode", "simple",
                    "query", query
                );
                
                // 使用正确的scenario值：general_chat
                saveConversation(conversationId, userId, "简单对话", "general_chat", contextObj);
                log.info("对话记录保存成功: conversationId={}", conversationId);
                
            } catch (Exception e) {
                log.error("保存简单对话记录失败: conversationId={}", conversationId, e);
                // 不影响主要功能，继续返回结果
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", response.getResult().getOutput().getText());
            result.put("conversationId", conversationId); // 返回实际的conversationId
            result.put("usage", Map.of(
                "promptTokens", response.getMetadata().getUsage().getPromptTokens(),
                "completionTokens", response.getMetadata().getUsage().getCompletionTokens(),
                "totalTokens", response.getMetadata().getUsage().getTotalTokens()
            ));
            result.put("responseTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("model", "qwen-plus");
            result.put("scenario", "general_chat"); // 修改为正确的scenario值
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Simple chat failed", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("对话失败: " + e.getMessage(), "/api/simple/chat"));
        }
    }


    /** 2.2 流式对话接口 */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam(value = "query", defaultValue = "你好") String query,
                                   @RequestParam(value = "chat-id", defaultValue = "1") String chatId,
                                   HttpServletResponse response) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/event-stream");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            
            return dashScopeChatClient.prompt(query)
                    .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatId))
                    .stream()
                    .content()
                    .doOnError(error -> log.error("Stream chat failed", error))
                    .onErrorReturn("对话出现错误，请重试");
                    
        } catch (Exception e) {
            log.error("Stream chat setup failed", e);
            return Flux.just("对话初始化失败，请重试");
        }
    }

    /** 2.3 图片分析接口 - URL方式 */
    @PostMapping("/image/analyze/url")
    public ResponseEntity<?> analyzeImageByUrl(@RequestParam(defaultValue = "请分析这张图片的内容")String prompt,
                                              @RequestParam String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("图片URL不能为空", "/api/image/analyze/url"));
            }
            
            List<Media>mediaList=List.of(new Media(MimeTypeUtils.IMAGE_JPEG,new URI(imageUrl)));
            //包含图片的用户消息
            UserMessage message=UserMessage.builder()
                    .text(prompt)
                    .media(mediaList)
                    .build();

            //设置消息格式为图片
            message.getMetadata().put(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);

            // 创建提示词，启用多模态模型
            Prompt chatPrompt = new Prompt(message,
                    DashScopeChatOptions.builder()
                            .withModel("qwen-vl-max-latest")  // 使用视觉模型
                            .withMultiModel(true)             // 启用多模态
                            .withVlHighResolutionImages(true) // 启用高分辨率图片处理
                            .withTemperature(0.7)
                            .build());

            ChatResponse response = dashScopeChatClient.prompt(chatPrompt).call().chatResponse();
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", response.getResult().getOutput().getText());
            result.put("imageUrl", imageUrl);
            result.put("usage", Map.of(
                "promptTokens", response.getMetadata().getUsage().getPromptTokens(),
                "completionTokens", response.getMetadata().getUsage().getCompletionTokens(),
                "totalTokens", response.getMetadata().getUsage().getTotalTokens()
            ));
            result.put("responseTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(result);

        }catch (Exception e) {
            log.error("图片分析失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("图片分析失败: 无法访问指定的图片URL", "/api/image/analyze/url"));
        }
    }

    /** 2.4 图片分析接口 - 文件上传方式 */
    @PostMapping("/image/analyze/upload")
    public ResponseEntity<?> analyzeImageByUpload(@RequestParam(defaultValue = "请分析这张图片的内容") String prompt,
                                                 @RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("图片文件不能为空", "/api/image/analyze/upload"));
            }
            
            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body(createErrorResponse("请上传图片文件", "/api/image/analyze/upload"));
            }
            
            // 文件大小限制 10MB
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(createErrorResponse("图片文件大小不能超过10MB", "/api/image/analyze/upload"));
            }

            // 创建包含图片的用户消息
            Media media = new Media(MimeTypeUtils.parseMimeType(file.getContentType()), file.getResource());
            UserMessage message = UserMessage.builder()
                    .text(prompt)
                    .media(media)
                    .build();

            // 设置消息格式为图片
            message.getMetadata().put(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);

            // 创建提示词，启用多模态模型
            Prompt chatPrompt = new Prompt(message,
                    DashScopeChatOptions.builder()
                            .withModel("qwen-vl-max-latest")  // 使用视觉模型
                            .withMultiModel(true)             // 启用多模态
                            .withVlHighResolutionImages(true) // 启用高分辨率图片处理
                            .withTemperature(0.7)
                            .build());

            // 调用模型进行图片分析
            ChatResponse response = dashScopeChatClient.prompt(chatPrompt).call().chatResponse();
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", response.getResult().getOutput().getText());
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("usage", Map.of(
                "promptTokens", response.getMetadata().getUsage().getPromptTokens(),
                "completionTokens", response.getMetadata().getUsage().getCompletionTokens(),
                "totalTokens", response.getMetadata().getUsage().getTotalTokens()
            ));
            result.put("responseTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("图片分析失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("图片分析失败: " + e.getMessage(), "/api/image/analyze/upload"));
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
    
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    // 添加保存对话的方法
    private void saveConversation(String conversationId, Integer userId, String title, String scenario, Object contextObj) {
        try {
            log.info("开始保存对话记录: conversationId={}, userId={}, title={}, scenario={}", 
                    conversationId, userId, title, scenario);
            
            ObjectMapper objectMapper = new ObjectMapper();
            String contextInfo = objectMapper.writeValueAsString(contextObj);
            
            log.info("上下文信息: {}", contextInfo);

            Conversation conversation = Conversation.builder()
                    .id(conversationId)
                    .userId(userId)
                    .title(title)
                    .scenario(scenario)
                    .contextInfo(contextInfo)
                    .totalMessages(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            log.info("准备插入的对话对象: {}", conversation);
            
            int result = conversationMapper.insert(conversation);
            log.info("插入结果: result={}", result);
            
            if (result > 0) {
                log.info("对话记录保存成功: conversationId={}", conversationId);
            } else {
                log.warn("对话记录保存失败，insert返回值为: {}", result);
            }

        } catch (Exception e) {
            log.error("保存对话记录失败: conversationId={}, error={}", conversationId, e.getMessage(), e);
            throw new RuntimeException("保存对话记录失败: " + e.getMessage());
        }
    }
}
