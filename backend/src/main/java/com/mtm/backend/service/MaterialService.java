package com.mtm.backend.service;

import com.mtm.backend.model.DTO.AudioUploadDTO;
import com.mtm.backend.model.DTO.DocumentUploadDTO;
import com.mtm.backend.model.VO.MaterialUploadVO;
import com.mtm.backend.model.VO.TranscriptionTaskVO;
import com.mtm.backend.repository.TeachingMaterial;
import com.mtm.backend.repository.TranscriptionTask;
import com.mtm.backend.repository.mapper.TeachingMaterialMapper;
import com.mtm.backend.repository.mapper.TranscriptionTaskMapper;
import com.mtm.backend.utils.OssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionModel;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialService {
    
    private final TeachingMaterialMapper teachingMaterialMapper;
    private final TranscriptionTaskMapper transcriptionTaskMapper;
    private final OssUtil ossUtil;
    private final AudioTranscriptionModel audioTranscriptionModel;
    
    /**
     * 上传学术文档
     */
    public MaterialUploadVO uploadDocument(MultipartFile file, DocumentUploadDTO uploadDTO, Integer userId) throws IOException {
        String materialId = generateMaterialId();
        
        // 上传到OSS
        String folder = String.format("documents/%d/%s", userId, uploadDTO.getDocumentType());
        String ossKey = ossUtil.uploadFile(file, folder);
        
        // 保存到数据库
        TeachingMaterial material = new TeachingMaterial();
        material.setId(materialId);
        material.setOriginalName(file.getOriginalFilename());
        material.setStoredFilename(extractFilenameFromOssKey(ossKey));
        material.setOssKey(ossKey);
        material.setContentType(file.getContentType());
        material.setFileSize(file.getSize());
        material.setMaterialType("document");
        material.setTitle(uploadDTO.getTitle() != null ? uploadDTO.getTitle() : file.getOriginalFilename());
        material.setDescription(uploadDTO.getDescription());
        material.setSubject(uploadDTO.getSubject());
        material.setCourseLevel(uploadDTO.getCourseLevel());
        material.setDocumentType(uploadDTO.getDocumentType());
        material.setKeywords(uploadDTO.getKeywords());
        material.setUserId(userId);
        material.setCreatedAt(new Date());
        material.setUpdatedAt(new Date());
        
        teachingMaterialMapper.insert(material);
        
        // 构建返回结果
        MaterialUploadVO result = new MaterialUploadVO();
        result.setId(materialId);
        result.setFilename(material.getStoredFilename());
        result.setOriginalName(material.getOriginalName());
        result.setSubject(material.getSubject());
        result.setCourseLevel(material.getCourseLevel());
        result.setDocumentType(material.getDocumentType());
        result.setSize(material.getFileSize());
        result.setContentType(material.getContentType());
        result.setKeywords(material.getKeywords() != null ? Arrays.asList(material.getKeywords().split(",")) : null);
        result.setUploadedAt(material.getCreatedAt());
        result.setDownloadUrl(ossUtil.generateUrl(ossKey));
        
        return result;
    }
    
    /**
     * 上传音频文件及转录
     */
    public Object uploadAudio(MultipartFile file, AudioUploadDTO uploadDTO, Integer userId) throws IOException {
        String materialId = generateMaterialId();
        
        // 上传到OSS
        String folder = String.format("audio/%d/%s", userId, uploadDTO.getAudioType() != null ? uploadDTO.getAudioType() : "general");
        String ossKey = ossUtil.uploadFile(file, folder);
        
        // 保存基本信息到数据库
        TeachingMaterial material = new TeachingMaterial();
        material.setId(materialId);
        material.setOriginalName(file.getOriginalFilename());
        material.setStoredFilename(extractFilenameFromOssKey(ossKey));
        material.setOssKey(ossKey);
        material.setContentType(file.getContentType());
        material.setFileSize(file.getSize());
        material.setMaterialType("audio");
        material.setSubject(uploadDTO.getSubject());
        material.setDescription(uploadDTO.getDescription());
        material.setLanguage(uploadDTO.getLanguage());
        material.setAudioType(uploadDTO.getAudioType());
        material.setSpeaker(uploadDTO.getSpeaker());
        material.setUserId(userId);
        material.setCreatedAt(new Date());
        material.setUpdatedAt(new Date());
        
        teachingMaterialMapper.insert(material);
        
        // 如果需要转录
        if (uploadDTO.getNeedTranscription()) {
            if ("sync".equals(uploadDTO.getTranscriptionMode())) {
                return performSyncTranscription(file, material, uploadDTO);
            } else {
                return performAsyncTranscription(material, uploadDTO);
            }
        } else {
            // 不需要转录，直接返回音频信息
            return buildAudioResult(material, null);
        }
    }
    
    /**
     * 同步转录
     */
    private MaterialUploadVO performSyncTranscription(MultipartFile file, TeachingMaterial material, AudioUploadDTO uploadDTO) {
        try {
            // 创建临时文件
            Path tempFile = Files.createTempFile("audio_", "_" + material.getOriginalName());
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            // 执行转录
            AudioTranscriptionResponse response = audioTranscriptionModel.call(
                new AudioTranscriptionPrompt(
                    new FileSystemResource(tempFile.toFile()),
                    DashScopeAudioTranscriptionOptions.builder()
                            .withModel("paraformer-realtime-v2")
                            .build()
                )
            );
            
            String transcriptionText = response.getResult().getOutput();
            
            // 更新数据库
            material.setTranscriptionText(transcriptionText);
            material.setUpdatedAt(new Date());
            teachingMaterialMapper.updateById(material);
            
            // 清理临时文件
            Files.deleteIfExists(tempFile);
            
            return buildAudioResult(material, transcriptionText);
            
        } catch (Exception e) {
            log.error("同步转录失败", e);
            throw new RuntimeException("音频转录失败: " + e.getMessage());
        }
    }
    
    /**
     * 异步转录
     */
    private TranscriptionTaskVO performAsyncTranscription(TeachingMaterial material, AudioUploadDTO uploadDTO) {
        String taskId = generateTaskId();
        
        // 创建转录任务
        TranscriptionTask task = new TranscriptionTask();
        task.setTaskId(taskId);
        task.setMaterialId(material.getId());
        task.setTranscriptionMode("async");
        task.setStatus("processing");
        task.setProgress(0);
        task.setEstimatedTime(120); // 预估2分钟
        task.setStartedAt(new Date());
        
        transcriptionTaskMapper.insert(task);
        
        // TODO: 这里应该启动异步任务处理转录
        // 暂时返回任务信息
        TranscriptionTaskVO result = new TranscriptionTaskVO();
        result.setTaskId(taskId);
        result.setMaterialId(material.getId());
        result.setMessage("语音转录任务已启动");
        result.setEstimatedTime(120);
        result.setStatus("processing");
        result.setProgress(0);
        result.setStatusUrl("/api/tasks/" + taskId + "/status");
        
        return result;
    }
    
    private MaterialUploadVO buildAudioResult(TeachingMaterial material, String transcriptionText) {
        MaterialUploadVO result = new MaterialUploadVO();
        result.setId(material.getId());
        result.setFilename(material.getStoredFilename());
        result.setOriginalName(material.getOriginalName());
        result.setSubject(material.getSubject());
        result.setAudioType(material.getAudioType());
        result.setDescription(material.getDescription());
        result.setSpeaker(material.getSpeaker());
        result.setDuration(material.getDuration());
        result.setSize(material.getFileSize());
        result.setLanguage(material.getLanguage());
        result.setUploadedAt(material.getCreatedAt());
        result.setDownloadUrl(ossUtil.generateUrl(material.getOssKey()));
        result.setTranscription(transcriptionText);
        
        return result;
    }
    
    private String generateMaterialId() {
        return "mat_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    private String generateTaskId() {
        return "task_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    private String extractFilenameFromOssKey(String ossKey) {
        return ossKey.substring(ossKey.lastIndexOf("/") + 1);
    }
}