package com.mtm.backend.service.preview;

import com.mtm.backend.model.VO.FilePreviewVO;
import com.mtm.backend.repository.TeachingResource;

/**
 * 文件预览策略接口
 * 支持不同类型文件的预览实现
 */
public interface FilePreviewStrategy {
    
    /**
     * 判断是否支持该文件类型
     * @param contentType 文件MIME类型
     * @return 是否支持
     */
    boolean supports(String contentType);
    
    /**
     * 生成文件预览信息
     * @param resource 资源信息
     * @return 预览信息
     */
    FilePreviewVO generatePreview(TeachingResource resource);
    
    /**
     * 获取预览类型
     * @return 预览类型
     */
    String getPreviewType();
}