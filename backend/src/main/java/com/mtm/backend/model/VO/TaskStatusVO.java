package com.mtm.backend.model.VO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusVO {
    private String taskId;
    private String status;
    private Integer progress;
    private Object result;
    private String error;
    private String createdAt;
    private String completedAt;
}