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
 * 图片预览策略
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ImagePreviewStrategy implements FilePreviewStrategy {
    
    private final LocalFileUtil localFileUtil;
    
    @Override
    public boolean supports(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
    
    @Override
    public FilePreviewVO generatePreview(TeachingResource resource) {
        try {
            String previewUrl = localFileUtil.generateUrl(resource.getFilePath());
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("width", "auto");
            metadata.put("height", "auto");
            metadata.put("size", resource.getFileSize());
            
            return FilePreviewVO.builder()
                    .previewType("image")
                    .previewUrl(previewUrl)
                    .originalUrl(previewUrl)
                    .previewable(true)
                    .metadata(metadata)
                    .build();
                    
        } catch (Exception e) {
            log.error("生成图片预览失败: {}", e.getMessage());
            return FilePreviewVO.builder()
                    .previewType("image")
                    .previewable(false)
                    .errorMessage("图片预览生成失败: " + e.getMessage())
                    .build();
        }
    }
    
    @Override
    public String getPreviewType() {
        return "image";
    }
}