package com.mtm.backend.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceListVO {
    private String id;
    private String title;
    private String subject;
    private String courseLevel;
    private String resourceType;
    private Long fileSize;
    private List<String> keywords;
    private Boolean isVectorized;
    private Date createdAt;
}