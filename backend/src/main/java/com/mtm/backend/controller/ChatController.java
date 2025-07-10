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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;

@RestController
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
    public String simpleChat(@RequestParam(value = "query",defaultValue = "你好,能简单介绍一下自己吗")String query,
                             @RequestParam(value = "chat-id",defaultValue = "1")String chatId) {
        log.info("simple chat query:{},chat-id:{}", query, chatId);
        return dashScopeChatClient.prompt(query)
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatId))
                .call()
                .content();
    }


    @GetMapping("/stream/chat")
    public Flux<String> streamChat(HttpServletResponse response) {

        response.setCharacterEncoding("UTF-8");
        return dashScopeChatClient.prompt(DEFAULT_PROMPT).stream().content();
    }

    /**
     * 图片分析接口 - 通过 URL
     */
    @PostMapping("/image/analyze/url")
    public String analyzeImageByUrl(@RequestParam(defaultValue = "请分析这张图片的内容")String prompt,
                                    @RequestParam String imageUrl) {
        try {
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

            return dashScopeChatClient.prompt(chatPrompt).call().content();

        }catch (Exception e) {
            return "图片分析失败"+ e.getMessage();
        }
    }

    /**
     * 图片分析接口 - 通过文件上传
     */
    @PostMapping("/image/analyze/upload")
    public String analyzeImageByUpload(@RequestParam(defaultValue = "请分析这张图片的内容") String prompt,
                                       @RequestParam("file") MultipartFile file) {
        try {
            // 验证文件类型
            if (!file.getContentType().startsWith("image/")) {
                return "请上传图片文件";
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
            return dashScopeChatClient.prompt(chatPrompt).call().content();

        } catch (Exception e) {
            return "图片分析失败: " + e.getMessage();
        }
    }


    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
