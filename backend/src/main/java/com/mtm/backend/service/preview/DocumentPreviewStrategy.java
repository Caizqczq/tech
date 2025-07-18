package com.mtm.backend.service.preview;

import com.mtm.backend.model.VO.FilePreviewVO;
import com.mtm.backend.repository.TeachingResource;
import com.mtm.backend.utils.LocalFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档预览策略
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentPreviewStrategy implements FilePreviewStrategy {
    
    private final LocalFileUtil localFileUtil;
    
    @Override
    public boolean supports(String contentType) {
        return contentType != null && (
            contentType.equals("application/pdf") ||
            contentType.equals("application/msword") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
            contentType.equals("application/vnd.ms-powerpoint") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
            contentType.equals("text/plain") ||
            contentType.equals("text/markdown")
        );
    }
    
    @Override
    public FilePreviewVO generatePreview(TeachingResource resource) {
        try {
            String previewType = determinePreviewType(resource.getContentType());
            String previewUrl = localFileUtil.generateUrl(resource.getFilePath());
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("size", resource.getFileSize());
            metadata.put("contentType", resource.getContentType());
            
            // 对于PDF文件，支持直接预览
            if ("application/pdf".equals(resource.getContentType())) {
                return FilePreviewVO.builder()
                        .previewType("pdf")
                        .previewUrl(previewUrl)
                        .originalUrl(previewUrl)
                        .previewable(true)
                        .metadata(metadata)
                        .build();
            }
            
            // 对于文本文件，尝试读取内容
            if ("text/plain".equals(resource.getContentType()) || "text/markdown".equals(resource.getContentType())) {
                return FilePreviewVO.builder()
                        .previewType("text")
                        .previewUrl(previewUrl)
                        .originalUrl(previewUrl)
                        .previewable(true)
                        .metadata(metadata)
                        .build();
            }
            
            // 对于其他Office文档，暂时返回不支持预览但提供下载
            return FilePreviewVO.builder()
                    .previewType("document")
                    .previewUrl(previewUrl)
                    .originalUrl(previewUrl)
                    .previewable(false)
                    .metadata(metadata)
                    .build();
                    
        } catch (Exception e) {
            log.error("生成文档预览失败: {}", e.getMessage());
            return FilePreviewVO.builder()
                    .previewType("document")
                    .previewable(false)
                    .errorMessage("文档预览生成失败: " + e.getMessage())
                    .build();
        }
    }
    
    private String determinePreviewType(String contentType) {
        if ("application/pdf".equals(contentType)) {
            return "pdf";
        } else if ("text/plain".equals(contentType) || "text/markdown".equals(contentType)) {
            return "text";
        } else {
            return "document";
        }
    }
    
    @Override
    public String getPreviewType() {
        return "document";
    }
}