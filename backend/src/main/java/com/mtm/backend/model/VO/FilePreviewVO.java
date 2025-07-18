package com.mtm.backend.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 文件预览信息VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilePreviewVO {
    
    /**
     * 预览类型: image, pdf, document, audio, video, text, unsupported
     */
    private String previewType;
    
    /**
     * 预览URL
     */
    private String previewUrl;
    
    /**
     * 原始文件URL
     */
    private String originalUrl;
    
    /**
     * 是否支持预览
     */
    private Boolean previewable;
    
    /**
     * 预览图片列表（用于PDF转图片等场景）
     */
    private List<String> previewImages;
    
    /**
     * 文本内容（用于文本文件或转录内容）
     */
    private String textContent;
    
    /**
     * 文件元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 错误信息
     */
    private String errorMessage;
}