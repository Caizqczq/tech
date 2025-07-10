package com.mtm.backend.repository;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("transcription_tasks")
public class TranscriptionTask {
    
    @TableId(type = IdType.INPUT)
    private String taskId;
    
    private String materialId;
    private String transcriptionMode;
    private String status;
    private Integer progress;
    private Integer estimatedTime;
    private String errorMessage;
    private Date startedAt;
    private Date completedAt;
}