package com.mtm.backend.controller;

import com.mtm.backend.model.VO.TaskStatusVO;
import com.mtm.backend.service.TaskService;
import com.mtm.backend.service.AIGenerationService;
import com.mtm.backend.model.DTO.PPTGenerationDTO;
import com.mtm.backend.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 文件下载和预览控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class FileDownloadController {

    private final TaskService taskService;
    private final AIGenerationService aiGenerationService;
    
    /**
     * 下载生成的文件
     */
    @GetMapping("/download/{taskId}")
    public ResponseEntity<?> downloadFile(@PathVariable String taskId) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("用户未登录"));
            }
            
            // 获取任务状态
            TaskStatusVO taskStatus = taskService.getTaskStatus(taskId, userId);
            
            if (!"completed".equals(taskStatus.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("任务未完成或已失败"));
            }
            
            // 获取文件数据
            Map<String, Object> result = (Map<String, Object>) taskStatus.getResult();
            if (result == null || !result.containsKey("fileData")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("文件数据不存在"));
            }
            
            byte[] fileData = (byte[]) result.get("fileData");
            String fileName = (String) result.getOrDefault("fileName", "generated_file.pptx");
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            // 设置正确的MIME类型为PowerPoint文件
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
            headers.setContentDispositionFormData("attachment",
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            headers.setContentLength(fileData.length);
            
            log.info("用户{}下载文件：{}，大小：{} bytes", userId, fileName, fileData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileData);
                    
        } catch (Exception e) {
            log.error("下载文件失败，任务ID：{}", taskId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("下载失败: " + e.getMessage()));
        }
    }
    
    /**
     * 预览生成的文件内容
     */
    @GetMapping("/preview/{taskId}")
    public ResponseEntity<?> previewFile(@PathVariable String taskId) {
        try {
            log.debug("开始预览文件，任务ID：{}", taskId);

            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                log.warn("用户未登录，任务ID：{}", taskId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(createErrorResponse("用户未登录"));
            }

            // 获取任务状态
            TaskStatusVO taskStatus = taskService.getTaskStatus(taskId, userId);
            log.debug("任务状态：{}，用户ID：{}", taskStatus.getStatus(), userId);

            if (!"completed".equals(taskStatus.getStatus())) {
                return ResponseEntity.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(createErrorResponse("任务未完成或已失败，当前状态：" + taskStatus.getStatus()));
            }

            // 获取文件内容
            Map<String, Object> result = (Map<String, Object>) taskStatus.getResult();
            if (result == null) {
                return ResponseEntity.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(createErrorResponse("任务结果不存在"));
            }

            // 构建预览响应
            Map<String, Object> previewData = Map.of(
                "taskId", taskId,
                "content", result.getOrDefault("content", ""),
                "fileName", result.getOrDefault("fileName", "generated_file.pptx"),
                "fileSize", result.getOrDefault("fileSize", 0),
                "topic", result.getOrDefault("topic", ""),
                "generatedAt", result.getOrDefault("generatedAt", "")
            );

            log.info("用户{}预览文件成功，任务ID：{}，文件名：{}",
                    userId, taskId, previewData.get("fileName"));

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(previewData);

        } catch (Exception e) {
            log.error("预览文件失败，任务ID：{}", taskId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createErrorResponse("预览失败: " + e.getMessage()));
        }
    }
    
    /**
     * 重新生成PPT
     */
    @PostMapping("/regenerate-ppt/{taskId}")
    public ResponseEntity<?> regeneratePPT(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> requestData) {
        try {
            log.debug("开始重新生成PPT，任务ID：{}", taskId);

            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                log.warn("用户未登录，任务ID：{}", taskId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(createErrorResponse("用户未登录"));
            }

            // 获取原始任务状态
            TaskStatusVO originalTask = taskService.getTaskStatus(taskId, userId);
            if (originalTask == null) {
                return ResponseEntity.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(createErrorResponse("原始任务不存在"));
            }

            // 获取原始任务结果
            Map<String, Object> originalResult = (Map<String, Object>) originalTask.getResult();
            if (originalResult == null) {
                return ResponseEntity.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(createErrorResponse("原始任务结果不存在"));
            }

            // 构建新的PPT生成请求
            PPTGenerationDTO newRequest = new PPTGenerationDTO();
            newRequest.setTopic((String) originalResult.get("topic"));
            newRequest.setSubject((String) originalResult.get("subject"));
            newRequest.setCourseLevel((String) originalResult.get("courseLevel"));
            newRequest.setSlideCount((Integer) originalResult.getOrDefault("slideCount", 10));
            newRequest.setStyle((String) originalResult.get("style"));

            // 使用编辑后的内容
            String newContent = (String) requestData.get("content");

            // 创建新任务
            String newTaskId = java.util.UUID.randomUUID().toString().replace("-", "");
            taskService.createTask(newTaskId, "ppt", userId, "重新生成PPT");

            // 异步生成新的PPT（使用编辑后的内容）
            aiGenerationService.regeneratePPTWithContent(newTaskId, newRequest, userId, newContent);

            // 构建响应
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "PPT重新生成任务已创建",
                "newTaskId", newTaskId,
                "originalTaskId", taskId
            );

            log.info("用户{}重新生成PPT成功，原任务ID：{}，新任务ID：{}", userId, taskId, newTaskId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (Exception e) {
            log.error("重新生成PPT失败，任务ID：{}", taskId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createErrorResponse("重新生成失败: " + e.getMessage()));
        }
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        return Map.of(
            "error", true,
            "message", message,
            "timestamp", System.currentTimeMillis()
        );
    }
}
