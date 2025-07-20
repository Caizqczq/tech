package com.mtm.backend.service.impl;

import com.mtm.backend.model.VO.TaskStatusVO;
import com.mtm.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TASK_PREFIX = "task:";
    private static final int TASK_EXPIRE_HOURS = 24;
    
    @Override
    public TaskStatusVO getTaskStatus(String taskId, Integer userId) {
        try {
            String key = TASK_PREFIX + taskId;
            Map<Object, Object> taskData = redisTemplate.opsForHash().entries(key);
            
            if (taskData.isEmpty()) {
                throw new RuntimeException("任务不存在或已过期");
            }
            
            // 验证任务所有者
            Integer taskUserId = (Integer) taskData.get("userId");
            if (!userId.equals(taskUserId)) {
                throw new RuntimeException("无权限访问该任务");
            }
            
            return TaskStatusVO.builder()
                    .taskId(taskId)
                    .status((String) taskData.get("status"))
                    .progress((Integer) taskData.getOrDefault("progress", 0))
                    .result(taskData.get("result"))
                    .error((String) taskData.get("error"))
                    .createdAt((String) taskData.get("createdAt"))
                    .completedAt((String) taskData.get("completedAt"))
                    .build();
                    
        } catch (Exception e) {
            log.error("获取任务状态失败，任务ID：{}", taskId, e);
            throw new RuntimeException("获取任务状态失败: " + e.getMessage());
        }
    }
    
    @Override
    public String createTask(String taskType, Object taskData, Integer userId) {
        try {
            String taskId = UUID.randomUUID().toString().replace("-", "");
            String key = TASK_PREFIX + taskId;
            
            Map<String, Object> task = new HashMap<>();
            task.put("taskId", taskId);
            task.put("taskType", taskType);
            task.put("status", "pending");
            task.put("progress", 0);
            task.put("userId", userId);
            task.put("taskData", taskData);
            task.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            redisTemplate.opsForHash().putAll(key, task);
            redisTemplate.expire(key, TASK_EXPIRE_HOURS, TimeUnit.HOURS);
            
            log.info("创建任务成功，任务ID：{}，类型：{}", taskId, taskType);
            return taskId;
            
        } catch (Exception e) {
            log.error("创建任务失败", e);
            throw new RuntimeException("创建任务失败: " + e.getMessage());
        }
    }
    
    @Override
    public void updateTaskStatus(String taskId, String status, int progress, String message) {
        try {
            String key = TASK_PREFIX + taskId;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            updates.put("progress", progress);
            updates.put("message", message);
            updates.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            if ("failed".equals(status)) {
                updates.put("error", message);
            }
            
            if ("completed".equals(status)) {
                updates.put("completedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            
            redisTemplate.opsForHash().putAll(key, updates);
            
            log.debug("更新任务状态，任务ID：{}，状态：{}，进度：{}%", taskId, status, progress);
            
        } catch (Exception e) {
            log.error("更新任务状态失败，任务ID：{}", taskId, e);
            throw new RuntimeException("更新任务状态失败: " + e.getMessage());
        }
    }
    
    @Override
    public void completeTask(String taskId, Object result) {
        try {
            String key = TASK_PREFIX + taskId;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "completed");
            updates.put("progress", 100);
            updates.put("result", result);
            updates.put("completedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            updates.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            redisTemplate.opsForHash().putAll(key, updates);
            
            log.info("任务完成，任务ID：{}", taskId);
            
        } catch (Exception e) {
            log.error("完成任务失败，任务ID：{}", taskId, e);
            throw new RuntimeException("完成任务失败: " + e.getMessage());
        }
    }

    @Override
    public void createTask(String taskId, String taskType, Integer userId, String description) {
        try {
            String key = TASK_PREFIX + taskId;

            Map<String, Object> taskData = new HashMap<>();
            taskData.put("taskId", taskId);
            taskData.put("taskType", taskType);
            taskData.put("userId", userId);
            taskData.put("description", description);
            taskData.put("status", "created");
            taskData.put("progress", 0);
            taskData.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            taskData.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            redisTemplate.opsForHash().putAll(key, taskData);
            redisTemplate.expire(key, Duration.ofHours(24));

            log.info("创建任务成功，任务ID：{}，类型：{}", taskId, taskType);

        } catch (Exception e) {
            log.error("创建任务失败，任务ID：{}", taskId, e);
            throw new RuntimeException("创建任务失败: " + e.getMessage());
        }
    }

    @Override
    public void failTask(String taskId, String errorMessage) {
        try {
            String key = TASK_PREFIX + taskId;

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "failed");
            updates.put("progress", 0);
            updates.put("error", errorMessage);
            updates.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            redisTemplate.opsForHash().putAll(key, updates);

            log.error("任务失败，任务ID：{}，错误：{}", taskId, errorMessage);

        } catch (Exception e) {
            log.error("标记任务失败时出错，任务ID：{}", taskId, e);
            throw new RuntimeException("标记任务失败时出错: " + e.getMessage());
        }
    }
}
