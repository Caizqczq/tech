package com.mtm.backend.model.VO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultVO {
    private String id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String status;
    private String message;
    private String createdAt;
}