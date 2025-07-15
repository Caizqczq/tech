package com.mtm.backend.model.VO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeItemVO {
    private String id;
    private String title;
    private String content;
    private String source;
    private String subject;
    private String grade;
    private List<String> tags;
    private Double similarity;
    private String createdAt;
}