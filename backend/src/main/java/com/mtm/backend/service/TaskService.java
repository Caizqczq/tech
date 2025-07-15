package com.mtm.backend.service;

import com.mtm.backend.model.VO.TaskStatusVO;

/**
 * 任务管理服务接口
 */
public interface TaskService {
    
    /**
     * 获取任务状态
     */
    TaskStatusVO getTaskStatus(String taskId, Integer userId);
    
    /**
     * 取消任务
     */
    void cancelTask(String taskId, Integer userId);
    
    /**
     * 创建新任务
     */
    String createTask(String taskType, Object taskData, Integer userId);
    
    /**
     * 更新任务状态
     */
    void updateTaskStatus(String taskId, String status, int progress, String message);
    
    /**
     * 完成任务
     */
    void completeTask(String taskId, Object result);
}
