package com.mtm.backend.service;

import com.mtm.backend.model.VO.KnowledgeItemVO;
import com.mtm.backend.model.VO.PaginationVO;
import com.mtm.backend.model.VO.UploadResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库服务接口
 */
public interface KnowledgeService {
    
    /**
     * 获取知识库项目列表
     */
    PaginationVO<KnowledgeItemVO> getKnowledgeItems(int page, int size, String subject, 
                                                   String category, String search, Integer userId);
    
    /**
     * 上传文档到知识库
     */
    UploadResultVO uploadDocument(MultipartFile file, String subject, String category, 
                                 String title, String content, String tags, Integer userId);
    
    /**
     * 删除知识库项目
     */
    void deleteKnowledgeItem(String itemId, Integer userId);
    
    /**
     * 更新知识库项目
     */
    KnowledgeItemVO updateKnowledgeItem(String itemId, String title, String content, 
                                       String tags, Integer userId);
}