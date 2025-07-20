package com.mtm.backend.service.impl;

import com.mtm.backend.model.DTO.ExplanationRequestDTO;
import com.mtm.backend.model.DTO.PPTGenerationDTO;
import com.mtm.backend.model.DTO.QuizGenerationDTO;
import com.mtm.backend.model.VO.TaskResponseVO;
import com.mtm.backend.service.AIGenerationService;
import com.mtm.backend.service.PPTGenerationService;
import com.mtm.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIGenerationServiceImpl implements AIGenerationService {

    private final ChatClient.Builder chatClientBuilder;
    private final TaskService taskService;
    private final PPTGenerationService pptGenerationService;
    
    @Override
    public TaskResponseVO generateExplanation(ExplanationRequestDTO request, Integer userId) {
        try {
            // 创建任务
            String taskId = taskService.createTask("explanation", request, userId);
            
            // 异步执行生成任务
            generateExplanationAsync(taskId, request, userId);
            
            return TaskResponseVO.builder()
                    .taskId(taskId)
                    .status("processing")
                    .message("讲解文本生成任务已启动")
                    .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
                    
        } catch (Exception e) {
            log.error("创建讲解生成任务失败", e);
            throw new RuntimeException("创建讲解生成任务失败: " + e.getMessage());
        }
    }
    
    @Async("aiGenerationTaskExecutor")
    public void generateExplanationAsync(String taskId, ExplanationRequestDTO request, Integer userId) {
        try {
            log.info("开始生成讲解内容，任务ID：{}，主题：{}", taskId, request.getTopic());
            
            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 10, "正在构建提示词...");
            
            // 构建系统提示词
            String systemPrompt = buildExplanationSystemPrompt(request);
            
            // 构建用户查询
            String userQuery = buildExplanationQuery(request);
            
            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 30, "正在调用AI模型...");
            
            // 创建ChatClient并生成内容
            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(systemPrompt)
                    .build();
            
            String explanation = chatClient.prompt(userQuery)
                    .call()
                    .content();
            
            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 80, "正在处理生成结果...");
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("content", explanation);
            result.put("topic", request.getTopic());
            result.put("subject", request.getSubject());
            result.put("courseLevel", request.getCourseLevel());
            result.put("style", request.getStyle());
            result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // 完成任务
            taskService.completeTask(taskId, result);
            
            log.info("讲解内容生成完成，任务ID：{}", taskId);
            
        } catch (Exception e) {
            log.error("生成讲解内容失败，任务ID：{}", taskId, e);
            taskService.updateTaskStatus(taskId, "failed", 0, "生成失败: " + e.getMessage());
        }
    }
    
    @Override
    public TaskResponseVO generatePPT(PPTGenerationDTO request, Integer userId) {
        try {
            // 创建任务
            String taskId = taskService.createTask("ppt", request, userId);
            
            // 异步执行生成任务
            generatePPTAsync(taskId, request, userId);
            
            return TaskResponseVO.builder()
                    .taskId(taskId)
                    .status("processing")
                    .message("PPT生成任务已启动")
                    .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
                    
        } catch (Exception e) {
            log.error("创建PPT生成任务失败", e);
            throw new RuntimeException("创建PPT生成任务失败: " + e.getMessage());
        }
    }
    
    @Override
    public TaskResponseVO generateQuiz(QuizGenerationDTO request, Integer userId) {
        try {
            // 创建任务
            String taskId = taskService.createTask("quiz", request, userId);
            
            // 异步执行生成任务
            generateQuizAsync(taskId, request, userId);
            
            return TaskResponseVO.builder()
                    .taskId(taskId)
                    .status("processing")
                    .message("习题生成任务已启动")
                    .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
                    
        } catch (Exception e) {
            log.error("创建习题生成任务失败", e);
            throw new RuntimeException("创建习题生成任务失败: " + e.getMessage());
        }
    }
    
    private String buildExplanationSystemPrompt(ExplanationRequestDTO request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的").append(request.getSubject()).append("教师，");
        prompt.append("擅长为").append(request.getCourseLevel()).append("学生提供清晰易懂的教学解释。\n\n");
        
        prompt.append("请根据以下要求生成教学内容：\n");
        prompt.append("- 学科：").append(request.getSubject()).append("\n");
        prompt.append("- 课程层次：").append(request.getCourseLevel()).append("\n");
        
        if (request.getStyle() != null) {
            prompt.append("- 讲解风格：").append(request.getStyle()).append("\n");
        }
        
        if (request.getLength() != null) {
            prompt.append("- 内容长度：").append(request.getLength()).append("\n");
        }
        
        if (request.getTargetAudience() != null) {
            prompt.append("- 目标受众：").append(request.getTargetAudience()).append("\n");
        }
        
        prompt.append("\n请确保内容：\n");
        prompt.append("1. 逻辑清晰，层次分明\n");
        prompt.append("2. 语言准确，表达规范\n");
        prompt.append("3. 适合目标学生的认知水平\n");
        
        if (request.getIncludeExamples() != null && request.getIncludeExamples()) {
            prompt.append("4. 包含具体的例子和应用\n");
        }
        
        if (request.getIncludeProofs() != null && request.getIncludeProofs()) {
            prompt.append("5. 包含必要的证明过程\n");
        }
        
        if (request.getIncludeApplications() != null && request.getIncludeApplications()) {
            prompt.append("6. 包含实际应用场景\n");
        }
        
        return prompt.toString();
    }
    
    private String buildExplanationQuery(ExplanationRequestDTO request) {
        return "请详细解释：" + request.getTopic();
    }
    
    @Async("aiGenerationTaskExecutor")
    public void generatePPTAsync(String taskId, PPTGenerationDTO request, Integer userId) {
        try {
            log.info("开始生成PPT，任务ID：{}，主题：{}", taskId, request.getTopic());
            
            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 10, "正在构建PPT提示词...");
            
            // 构建系统提示词
            String systemPrompt = buildPPTSystemPrompt(request);
            
            // 构建用户查询
            String userQuery = buildPPTQuery(request);
            
            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 30, "正在调用AI模型生成PPT...");
            
            // 创建ChatClient并生成内容
            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(systemPrompt)
                    .build();
            
            String pptContent = chatClient.prompt(userQuery)
                    .call()
                    .content();
            
            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 60, "正在生成PPT文件...");

            // 生成实际的PPT文件
            byte[] pptBytes = pptGenerationService.generatePPTFile(pptContent, request);

            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 80, "正在保存PPT文件...");

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("content", pptContent);
            result.put("fileData", pptBytes);
            result.put("fileName", generateFileName(request.getTopic(), "pptx"));
            result.put("fileSize", pptBytes.length);
            result.put("topic", request.getTopic());
            result.put("subject", request.getSubject());
            result.put("courseLevel", request.getCourseLevel());
            result.put("slideCount", request.getSlideCount());
            result.put("style", request.getStyle());
            result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // 完成任务
            taskService.completeTask(taskId, result);
            
            log.info("PPT生成完成，任务ID：{}", taskId);
            
        } catch (Exception e) {
            log.error("生成PPT失败，任务ID：{}", taskId, e);
            taskService.updateTaskStatus(taskId, "failed", 0, "生成失败: " + e.getMessage());
        }
    }
    
    @Async("aiGenerationTaskExecutor")
    public void generateQuizAsync(String taskId, QuizGenerationDTO request, Integer userId) {
        try {
            log.info("开始生成习题，任务ID：{}，主题：{}", taskId, request.getTopic());
            
            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 10, "正在构建习题提示词...");
            
            // 构建系统提示词
            String systemPrompt = buildQuizSystemPrompt(request);
            
            // 构建用户查询
            String userQuery = buildQuizQuery(request);
            
            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 30, "正在调用AI模型生成习题...");
            
            // 创建ChatClient并生成内容
            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(systemPrompt)
                    .build();
            
            String quizContent = chatClient.prompt(userQuery)
                    .call()
                    .content();
            
            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 80, "正在处理习题生成结果...");
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("content", quizContent);
            result.put("topic", request.getTopic());
            result.put("subject", request.getSubject());
            result.put("courseLevel", request.getCourseLevel());
            result.put("difficulty", request.getDifficulty());
            result.put("questionCount", request.getQuestionCount());
            result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // 完成任务
            taskService.completeTask(taskId, result);
            
            log.info("习题生成完成，任务ID：{}", taskId);
            
        } catch (Exception e) {
            log.error("生成习题失败，任务ID：{}", taskId, e);
            taskService.updateTaskStatus(taskId, "failed", 0, "生成失败: " + e.getMessage());
        }
    }
    
    private String buildPPTSystemPrompt(PPTGenerationDTO request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的").append(request.getSubject()).append("教师，");
        prompt.append("擅长制作精美的教学PPT课件。\n\n");
        
        prompt.append("请根据以下要求生成PPT内容：\n");
        prompt.append("- 学科：").append(request.getSubject()).append("\n");
        prompt.append("- 课程层次：").append(request.getCourseLevel()).append("\n");
        prompt.append("- 幻灯片数量：").append(request.getSlideCount()).append("张\n");
        
        if (request.getStyle() != null) {
            prompt.append("- PPT风格：").append(request.getStyle()).append("\n");
        }
        
        if (request.getTargetAudience() != null) {
            prompt.append("- 目标受众：").append(request.getTargetAudience()).append("\n");
        }
        
        if (request.getDuration() != null) {
            prompt.append("- 预计时长：").append(request.getDuration()).append("分钟\n");
        }
        
        prompt.append("\n请确保PPT内容：\n");
        prompt.append("1. 结构清晰，逻辑合理\n");
        prompt.append("2. 每张幻灯片内容适中，不要过于拥挤\n");
        prompt.append("3. 包含标题、要点、总结等完整结构\n");
        
        if (request.getIncludeFormulas() != null && request.getIncludeFormulas()) {
            prompt.append("4. 包含相关的公式和计算\n");
        }
        
        if (request.getIncludeProofs() != null && request.getIncludeProofs()) {
            prompt.append("5. 包含必要的证明过程\n");
        }
        
        prompt.append("\n请按照以下格式输出每张幻灯片：\n");
        prompt.append("【幻灯片X】标题\n内容要点\n\n");
        
        return prompt.toString();
    }
    
    private String buildPPTQuery(PPTGenerationDTO request) {
        return "请为主题\"" + request.getTopic() + "\"制作一套完整的PPT课件。";
    }
    
    private String buildQuizSystemPrompt(QuizGenerationDTO request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的").append(request.getSubject()).append("教师，");
        prompt.append("擅长出题和评估学生学习效果。\n\n");
        
        prompt.append("请根据以下要求生成习题：\n");
        prompt.append("- 学科：").append(request.getSubject()).append("\n");
        prompt.append("- 课程层次：").append(request.getCourseLevel()).append("\n");
        prompt.append("- 难度级别：").append(request.getDifficulty()).append("\n");
        prompt.append("- 题目数量：").append(request.getQuestionCount()).append("题\n");
        
        if (request.getQuestionTypes() != null) {
            prompt.append("- 题目类型：").append(request.getQuestionTypes()).append("\n");
        }
        
        if (request.getTimeLimit() != null) {
            prompt.append("- 时间限制：").append(request.getTimeLimit()).append("分钟\n");
        }
        
        prompt.append("\n请确保习题：\n");
        prompt.append("1. 难度适中，符合学生水平\n");
        prompt.append("2. 题目表述清晰，无歧义\n");
        prompt.append("3. 涵盖主要知识点\n");
        
        if (request.getIncludeSteps() != null && request.getIncludeSteps()) {
            prompt.append("4. 包含详细的解题步骤\n");
        }
        
        if (request.getIncludeAnswers() != null && request.getIncludeAnswers()) {
            prompt.append("5. 包含标准答案\n");
        }
        
        prompt.append("\n请按照以下格式输出每道题：\n");
        prompt.append("【题目X】\n题目内容\n\n【答案X】\n答案内容\n\n");
        
        return prompt.toString();
    }
    
    private String buildQuizQuery(QuizGenerationDTO request) {
        return "请为主题\"" + request.getTopic() + "\"出一套完整的习题。";
    }

    /**
     * 使用编辑后的内容重新生成PPT
     */
    @Override
    @Async("aiGenerationTaskExecutor")
    public void regeneratePPTWithContent(String taskId, PPTGenerationDTO request, Integer userId, String editedContent) {
        log.info("开始重新生成PPT，任务ID：{}，主题：{}", taskId, request.getTopic());

        try {
            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 20, "正在处理编辑后的内容...");

            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 60, "正在生成PPT文件...");

            // 直接使用编辑后的内容生成PPT文件
            byte[] pptBytes = pptGenerationService.generatePPTFile(editedContent, request);

            // 更新任务状态
            taskService.updateTaskStatus(taskId, "processing", 80, "正在保存PPT文件...");

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("content", editedContent);
            result.put("fileData", pptBytes);
            result.put("fileName", generateFileName(request.getTopic() + "_编辑版", "pptx"));
            result.put("fileSize", pptBytes.length);
            result.put("topic", request.getTopic());
            result.put("subject", request.getSubject());
            result.put("courseLevel", request.getCourseLevel());
            result.put("slideCount", request.getSlideCount());
            result.put("style", request.getStyle());
            result.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("isRegenerated", true);

            // 完成任务
            taskService.completeTask(taskId, result);

            log.info("PPT重新生成完成，任务ID：{}，文件大小：{} bytes", taskId, pptBytes.length);

        } catch (Exception e) {
            log.error("PPT重新生成失败，任务ID：{}", taskId, e);
            taskService.failTask(taskId, "PPT重新生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String topic, String extension) {
        // 清理主题名称，移除特殊字符
        String cleanTopic = topic.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", "")
                                 .replaceAll("\\s+", "_")
                                 .trim();

        // 限制长度
        if (cleanTopic.length() > 50) {
            cleanTopic = cleanTopic.substring(0, 50);
        }

        // 添加时间戳
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        return cleanTopic + "_" + timestamp + "." + extension;
    }
}