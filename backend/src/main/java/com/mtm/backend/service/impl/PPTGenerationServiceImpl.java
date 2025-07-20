package com.mtm.backend.service.impl;

import com.mtm.backend.model.DTO.PPTGenerationDTO;
import com.mtm.backend.service.PPTGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PPT文件生成服务实现
 */
@Service
@Slf4j
public class PPTGenerationServiceImpl implements PPTGenerationService {

    @Override
    public byte[] generatePPTFile(String content, PPTGenerationDTO request) throws IOException {
        log.info("开始生成PPT文件，主题：{}", request.getTopic());

        XMLSlideShow ppt = null;
        ByteArrayOutputStream outputStream = null;

        try {
            // 解析AI生成的内容
            PPTSlideData slideData = parsePPTContent(content);
            log.debug("解析得到{}张幻灯片", slideData.getSlides().size());

            // 创建PPT
            ppt = new XMLSlideShow();

            // 创建标题页
            createTitleSlide(ppt, request.getTopic(), request.getSubject());
            log.debug("标题页创建完成");

            // 创建内容页
            for (int i = 0; i < slideData.getSlides().size(); i++) {
                PPTSlide slide = slideData.getSlides().get(i);
                createContentSlide(ppt, slide.getTitle(), slide.getContent());
                log.debug("内容页{}创建完成：{}", i + 1, slide.getTitle());
            }

            // 转换为字节数组
            outputStream = new ByteArrayOutputStream();
            ppt.write(outputStream);

            byte[] pptBytes = outputStream.toByteArray();

            log.info("PPT文件生成完成，大小：{} bytes，共{}张幻灯片",
                    pptBytes.length, slideData.getSlides().size() + 1);

            return pptBytes;

        } catch (Exception e) {
            log.error("PPT文件生成失败", e);
            throw new IOException("PPT文件生成失败: " + e.getMessage(), e);
        } finally {
            // 确保资源被正确关闭
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.warn("关闭输出流失败", e);
                }
            }
            if (ppt != null) {
                try {
                    ppt.close();
                } catch (IOException e) {
                    log.warn("关闭PPT对象失败", e);
                }
            }
        }
    }

    @Override
    public PPTSlideData parsePPTContent(String content) {
        log.debug("开始解析PPT内容");
        
        List<PPTSlide> slides = new ArrayList<>();
        
        // 使用正则表达式匹配幻灯片格式：【幻灯片X】标题
        Pattern slidePattern = Pattern.compile("【幻灯片\\d+】(.+?)(?=【幻灯片\\d+】|$)", Pattern.DOTALL);
        Matcher matcher = slidePattern.matcher(content);
        
        while (matcher.find()) {
            String slideContent = matcher.group(1).trim();
            PPTSlide slide = parseSlideContent(slideContent);
            if (slide != null) {
                slides.add(slide);
            }
        }
        
        // 如果没有匹配到标准格式，尝试按段落分割
        if (slides.isEmpty()) {
            slides = parseContentByParagraphs(content);
        }
        
        log.debug("解析完成，共{}张幻灯片", slides.size());
        return new PPTSlideData("AI生成的PPT", slides);
    }
    
    /**
     * 解析单个幻灯片内容
     */
    private PPTSlide parseSlideContent(String content) {
        String[] lines = content.split("\n");
        if (lines.length == 0) return null;
        
        String title = lines[0].trim();
        List<String> contentList = new ArrayList<>();
        
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                contentList.add(line);
            }
        }
        
        return new PPTSlide(title, contentList);
    }
    
    /**
     * 按段落解析内容（备用方案）
     */
    private List<PPTSlide> parseContentByParagraphs(String content) {
        List<PPTSlide> slides = new ArrayList<>();
        String[] paragraphs = content.split("\n\n");
        
        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i].trim();
            if (!paragraph.isEmpty()) {
                String[] lines = paragraph.split("\n");
                String title = lines.length > 0 ? lines[0] : "幻灯片 " + (i + 1);
                List<String> contentList = new ArrayList<>();
                
                for (int j = 1; j < lines.length; j++) {
                    String line = lines[j].trim();
                    if (!line.isEmpty()) {
                        contentList.add(line);
                    }
                }
                
                slides.add(new PPTSlide(title, contentList));
            }
        }
        
        return slides;
    }
    
    /**
     * 创建标题页
     */
    private void createTitleSlide(XMLSlideShow ppt, String title, String subtitle) {
        XSLFSlide slide = ppt.createSlide();

        // 创建标题文本框
        XSLFTextBox titleBox = slide.createTextBox();
        titleBox.setAnchor(new Rectangle2D.Double(50, 100, 600, 100));
        XSLFTextParagraph titleParagraph = titleBox.addNewTextParagraph();
        titleParagraph.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun titleRun = titleParagraph.addNewTextRun();
        titleRun.setText(title);
        titleRun.setFontSize(36.0);
        titleRun.setBold(true);
        titleRun.setFontColor(Color.DARK_GRAY);

        // 创建副标题文本框
        XSLFTextBox subtitleBox = slide.createTextBox();
        subtitleBox.setAnchor(new Rectangle2D.Double(50, 220, 600, 50));
        XSLFTextParagraph subtitleParagraph = subtitleBox.addNewTextParagraph();
        subtitleParagraph.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun subtitleRun = subtitleParagraph.addNewTextRun();
        subtitleRun.setText(subtitle);
        subtitleRun.setFontSize(24.0);
        subtitleRun.setFontColor(Color.GRAY);
    }
    
    /**
     * 创建内容页
     */
    private void createContentSlide(XMLSlideShow ppt, String title, List<String> content) {
        XSLFSlide slide = ppt.createSlide();

        // 创建标题
        XSLFTextBox titleBox = slide.createTextBox();
        titleBox.setAnchor(new Rectangle2D.Double(50, 50, 600, 60));
        XSLFTextParagraph titleParagraph = titleBox.addNewTextParagraph();
        XSLFTextRun titleRun = titleParagraph.addNewTextRun();
        titleRun.setText(title);
        titleRun.setFontSize(28.0);
        titleRun.setBold(true);
        titleRun.setFontColor(Color.DARK_GRAY);

        // 创建内容
        XSLFTextBox contentBox = slide.createTextBox();
        contentBox.setAnchor(new Rectangle2D.Double(50, 130, 600, 400));

        // 确保有内容才创建段落
        if (!content.isEmpty()) {
            for (int i = 0; i < content.size(); i++) {
                String line = content.get(i);
                if (line != null && !line.trim().isEmpty()) {
                    XSLFTextParagraph paragraph;
                    if (i == 0) {
                        // 第一个段落已经存在
                        paragraph = contentBox.getTextParagraphs().get(0);
                    } else {
                        // 添加新段落
                        paragraph = contentBox.addNewTextParagraph();
                    }
                    paragraph.setBullet(true);
                    paragraph.setIndentLevel(0);
                    XSLFTextRun run = paragraph.addNewTextRun();
                    run.setText(line.trim());
                    run.setFontSize(18.0);
                    run.setFontColor(Color.BLACK);
                }
            }
        } else {
            // 如果没有内容，添加一个默认段落
            XSLFTextParagraph paragraph = contentBox.getTextParagraphs().get(0);
            XSLFTextRun run = paragraph.addNewTextRun();
            run.setText("暂无内容");
            run.setFontSize(18.0);
            run.setFontColor(Color.GRAY);
        }
    }
}
