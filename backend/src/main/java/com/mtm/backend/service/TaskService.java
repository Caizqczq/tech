package com.mtm.backend.service;

import com.mtm.backend.model.VO.TranscriptionTaskVO;
import com.mtm.backend.repository.TranscriptionTask;
import com.mtm.backend.repository.mapper.TranscriptionTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TranscriptionTaskMapper transcriptionTaskMapper;
    
    /**
     * 获取任务状态
     */
    public Object getTaskStatus(String taskId) {
        // 查询转录任务
        TranscriptionTask task = transcriptionTaskMapper.selectById(taskId);
        if (task == null) {
            return null;
        }
        
        TranscriptionTaskVO result = new TranscriptionTaskVO();
        result.setTaskId(task.getTaskId());
        result.setStatus(task.getStatus());
        result.setProgress(task.getProgress());
        
        if ("processing".equals(task.getStatus())) {
            result.setMessage("正在处理语音转录");
        } else if ("completed".equals(task.getStatus())) {
            result.setMessage("转录完成");
            // TODO: 这里应该返回转录结果
        } else if ("failed".equals(task.getStatus())) {
            result.setMessage("转录失败: " + task.getErrorMessage());
        }
        
        return result;
    }
}