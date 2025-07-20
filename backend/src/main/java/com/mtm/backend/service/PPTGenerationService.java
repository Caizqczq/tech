package com.mtm.backend.service;

import com.mtm.backend.model.DTO.PPTGenerationDTO;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

import java.io.IOException;

/**
 * PPT文件生成服务接口
 */
public interface PPTGenerationService {
    
    /**
     * 根据AI生成的内容创建PPT文件
     * 
     * @param content AI生成的PPT内容文本
     * @param request 原始请求参数
     * @return PPT文件的字节数组
     * @throws IOException 文件生成异常
     */
    byte[] generatePPTFile(String content, PPTGenerationDTO request) throws IOException;
    
    /**
     * 解析AI生成的文本内容为结构化数据
     * 
     * @param content AI生成的内容
     * @return 解析后的幻灯片数据
     */
    PPTSlideData parsePPTContent(String content);
    
    /**
     * PPT幻灯片数据结构
     */
    class PPTSlideData {
        private String title;
        private java.util.List<PPTSlide> slides;
        
        public PPTSlideData() {}
        
        public PPTSlideData(String title, java.util.List<PPTSlide> slides) {
            this.title = title;
            this.slides = slides;
        }
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public java.util.List<PPTSlide> getSlides() { return slides; }
        public void setSlides(java.util.List<PPTSlide> slides) { this.slides = slides; }
    }
    
    /**
     * 单个幻灯片数据
     */
    class PPTSlide {
        private String title;
        private java.util.List<String> content;
        
        public PPTSlide() {}
        
        public PPTSlide(String title, java.util.List<String> content) {
            this.title = title;
            this.content = content;
        }
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public java.util.List<String> getContent() { return content; }
        public void setContent(java.util.List<String> content) { this.content = content; }
    }
}
