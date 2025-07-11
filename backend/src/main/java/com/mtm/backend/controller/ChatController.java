package com.mtm.backend.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api")
@Slf4j
public class ChatController {

    private static final String DEFAULT_PROMPT = "不要返回markdown";

    private final ChatClient dashScopeChatClient;
    private final JdbcTemplate jdbcTemplate;


    public ChatController(ChatModel chatModel, JdbcTemplate jdbcTemplate) {

        ChatMemoryRepository chatMemoryRepository=MysqlChatMemoryRepository.mysqlBuilder()
                .jdbcTemplate(jdbcTemplate)
                .build();
        ChatMemory chatMemory= MessageWindowChatMemory.builder()
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
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/simple/chat")
    public ResponseEntity<?> simpleChat(@RequestParam(value = "query",defaultValue = "你好,能简单介绍一下自己吗")String query,
                                       @RequestParam(value = "chat-id",defaultValue = "1")String chatId) {
        try {
            log.info("simple chat query:{},chat-id:{}", query, chatId);
            
            ChatResponse response = dashScopeChatClient.prompt(query)
                    .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatId))
                    .call().chatResponse();
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", response.getResult().getOutput().getText());
            result.put("conversationId", chatId);
            result.put("usage", Map.of(
                "promptTokens", response.getMetadata().getUsage().getPromptTokens(),
                "completionTokens", response.getMetadata().getUsage().getCompletionTokens(),
                "totalTokens", response.getMetadata().getUsage().getTotalTokens()
            ));
            result.put("responseTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Simple chat failed", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("对话失败: " + e.getMessage(), "/api/simple/chat"));
        }
    }


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

    /**
     * 图片分析接口 - 通过 URL
     */
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

    /**
     * 图片分析接口 - 通过文件上传
     */
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
}
