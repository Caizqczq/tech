package com.mtm.backend.service.impl;

import com.mtm.backend.model.DTO.ExplanationRequestDTO;
import com.mtm.backend.model.VO.TaskResponseVO;
import com.mtm.backend.service.AIGenerationService;
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
    
    @Async
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
    public TaskResponseVO generatePPT(Object request, Integer userId) {
        // TODO: 实现PPT生成逻辑
        throw new UnsupportedOperationException("PPT生成功能暂未实现");
    }
    
    @Override
    public TaskResponseVO generateQuiz(Object request, Integer userId) {
        // TODO: 实现习题生成逻辑
        throw new UnsupportedOperationException("习题生成功能暂未实现");
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
}