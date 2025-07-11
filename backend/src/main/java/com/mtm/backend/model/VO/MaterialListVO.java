package com.mtm.backend.model.VO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialListVO {
    private String materialId;
    private String originalName;
    private String materialType;
    private String contentType;
    private Long fileSize;
    private String title;
    private String subject;
    private String courseLevel;
    private String documentType;
    private String audioType;
    private Date createdAt;
}