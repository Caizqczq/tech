package com.mtm.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtm.backend.model.DTO.AudioUploadDTO;
import com.mtm.backend.model.DTO.DocumentUploadDTO;
import com.mtm.backend.model.DTO.MaterialQueryDTO;
import com.mtm.backend.model.VO.MaterialUploadVO;
import com.mtm.backend.model.VO.MaterialDetailVO;
import com.mtm.backend.model.VO.MaterialListVO;
import com.mtm.backend.model.VO.TranscriptionTaskVO;
import com.mtm.backend.repository.TeachingMaterial;
import com.mtm.backend.repository.TranscriptionTask;
import com.mtm.backend.repository.mapper.TeachingMaterialMapper;
import com.mtm.backend.repository.mapper.TranscriptionTaskMapper;
import com.mtm.backend.utils.OssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.cloud.ai.dashscope.audio.transcription.AudioTranscriptionModel;
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
import java.util.*;
import java.util.stream.Collectors;

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
            // 使用OSS带签名的公开URL进行转录，确保DashScope能够访问
            String signedUrl = ossUtil.generateSignedUrl(material.getOssKey(), 2); // 2小时过期
            
            log.info("开始音频转录，使用带签名的OSS URL: {}", signedUrl);
            
            // 执行转录
            AudioTranscriptionResponse response = audioTranscriptionModel.call(
                new AudioTranscriptionPrompt(
                    signedUrl,
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
            
            log.info("音频转录成功，材料ID: {}, 转录长度: {} 字符", material.getId(), transcriptionText.length());
            
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
    
    /**
     * 分页查询用户的教学素材列表
     */
    public Map<String, Object> getUserMaterials(MaterialQueryDTO queryDTO, Integer userId) {
        try {
            // 构建查询条件
            QueryWrapper<TeachingMaterial> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            
            if (queryDTO.getMaterialType() != null && !queryDTO.getMaterialType().trim().isEmpty()) {
                queryWrapper.eq("material_type", queryDTO.getMaterialType());
            }
            
            if (queryDTO.getSubject() != null && !queryDTO.getSubject().trim().isEmpty()) {
                queryWrapper.eq("subject", queryDTO.getSubject());
            }
            
            if (queryDTO.getCourseLevel() != null && !queryDTO.getCourseLevel().trim().isEmpty()) {
                queryWrapper.eq("course_level", queryDTO.getCourseLevel());
            }
            
            if (queryDTO.getDocumentType() != null && !queryDTO.getDocumentType().trim().isEmpty()) {
                queryWrapper.eq("document_type", queryDTO.getDocumentType());
            }
            
            if (queryDTO.getAudioType() != null && !queryDTO.getAudioType().trim().isEmpty()) {
                queryWrapper.eq("audio_type", queryDTO.getAudioType());
            }
            
            if (queryDTO.getKeywords() != null && !queryDTO.getKeywords().trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                    .like("title", queryDTO.getKeywords())
                    .or()
                    .like("description", queryDTO.getKeywords())
                    .or()
                    .like("keywords", queryDTO.getKeywords())
                );
            }
            
            // 排序
            String sortBy = queryDTO.getSortBy() != null ? queryDTO.getSortBy() : "created_at";
            String sortOrder = queryDTO.getSortOrder() != null ? queryDTO.getSortOrder() : "desc";
            
            if ("asc".equals(sortOrder)) {
                queryWrapper.orderByAsc(sortBy);
            } else {
                queryWrapper.orderByDesc(sortBy);
            }
            
            // 分页查询
            Page<TeachingMaterial> pageInfo = new Page<>(
                queryDTO.getPage() != null ? queryDTO.getPage() : 1,
                queryDTO.getLimit() != null ? queryDTO.getLimit() : 10
            );
            
            IPage<TeachingMaterial> materialPage = teachingMaterialMapper.selectPage(pageInfo, queryWrapper);
            
            // 转换为VO
            List<MaterialListVO> materialList = materialPage.getRecords().stream()
                    .map(this::convertToMaterialListVO)
                    .collect(Collectors.toList());
            
            return Map.of(
                "materials", materialList,
                "pagination", Map.of(
                    "currentPage", materialPage.getCurrent(),
                    "pageSize", materialPage.getSize(),
                    "total", materialPage.getTotal(),
                    "totalPages", materialPage.getPages()
                )
            );
            
        } catch (Exception e) {
            log.error("查询教学素材失败", e);
            throw new RuntimeException("查询教学素材失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取教学素材详情
     */
    public MaterialDetailVO getMaterialDetail(String materialId, Integer userId) {
        try {
            // 查询素材
            TeachingMaterial material = teachingMaterialMapper.selectById(materialId);
            if (material == null) {
                throw new RuntimeException("素材不存在");
            }
            
            // 验证权限
            if (!material.getUserId().equals(userId)) {
                throw new RuntimeException("无权访问该素材");
            }
            
            return convertToMaterialDetailVO(material);
            
        } catch (Exception e) {
            log.error("获取素材详情失败", e);
            throw new RuntimeException("获取素材详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除教学素材
     */
    public void deleteMaterial(String materialId, Integer userId) {
        try {
            // 查询素材
            TeachingMaterial material = teachingMaterialMapper.selectById(materialId);
            if (material == null) {
                throw new RuntimeException("素材不存在");
            }
            
            // 验证权限
            if (!material.getUserId().equals(userId)) {
                throw new RuntimeException("无权删除该素材");
            }
            
            // 删除OSS文件
            try {
                ossUtil.deleteFile(material.getOssKey());
            } catch (Exception e) {
                log.warn("删除OSS文件失败: {}", e.getMessage());
            }
            
            // 删除数据库记录
            teachingMaterialMapper.deleteById(materialId);
            
            // 如果是音频，删除相关转录任务
            if ("audio".equals(material.getMaterialType())) {
                QueryWrapper<TranscriptionTask> taskQuery = new QueryWrapper<>();
                taskQuery.eq("material_id", materialId);
                transcriptionTaskMapper.delete(taskQuery);
            }
            
        } catch (Exception e) {
            log.error("删除素材失败", e);
            throw new RuntimeException("删除素材失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取素材下载链接
     */
    public String getMaterialDownloadUrl(String materialId, Integer userId) {
        try {
            // 查询素材
            TeachingMaterial material = teachingMaterialMapper.selectById(materialId);
            if (material == null) {
                throw new RuntimeException("素材不存在");
            }
            
            // 验证权限
            if (!material.getUserId().equals(userId)) {
                throw new RuntimeException("无权访问该素材");
            }
            
            // 生成下载链接
            return ossUtil.generateUrl(material.getOssKey());
            
        } catch (Exception e) {
            log.error("获取下载链接失败", e);
            throw new RuntimeException("获取下载链接失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户素材统计信息
     */
    public Map<String, Object> getUserMaterialStatistics(Integer userId) {
        try {
            // 总素材数
            QueryWrapper<TeachingMaterial> totalQuery = new QueryWrapper<>();
            totalQuery.eq("user_id", userId);
            Long totalMaterials = teachingMaterialMapper.selectCount(totalQuery);
            
            // 按类型统计
            Map<String, Long> typeStats = Map.of(
                "document", getMaterialCountByType(userId, "document"),
                "audio", getMaterialCountByType(userId, "audio")
            );
            
            // 按学科统计
            List<Map<String, Object>> subjectStats = getSubjectStatistics(userId);
            
            // 今日上传数
            QueryWrapper<TeachingMaterial> todayQuery = new QueryWrapper<>();
            todayQuery.eq("user_id", userId);
            todayQuery.ge("created_at", new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
            Long todayMaterials = teachingMaterialMapper.selectCount(todayQuery);
            
            return Map.of(
                "totalMaterials", totalMaterials,
                "typeStatistics", typeStats,
                "subjectStatistics", subjectStats,
                "todayMaterials", todayMaterials
            );
            
        } catch (Exception e) {
            log.error("获取素材统计失败", e);
            throw new RuntimeException("获取素材统计失败: " + e.getMessage());
        }
    }
    
    // ============ 私有辅助方法 ============
    
    private MaterialListVO convertToMaterialListVO(TeachingMaterial material) {
        return MaterialListVO.builder()
                .materialId(material.getId())
                .originalName(material.getOriginalName())
                .materialType(material.getMaterialType())
                .contentType(material.getContentType())
                .fileSize(material.getFileSize())
                .title(material.getTitle())
                .subject(material.getSubject())
                .courseLevel(material.getCourseLevel())
                .documentType(material.getDocumentType())
                .audioType(material.getAudioType())
                .createdAt(material.getCreatedAt())
                .build();
    }
    
    private MaterialDetailVO convertToMaterialDetailVO(TeachingMaterial material) {
        return MaterialDetailVO.builder()
                .materialId(material.getId())
                .originalName(material.getOriginalName())
                .materialType(material.getMaterialType())
                .contentType(material.getContentType())
                .fileSize(material.getFileSize())
                .downloadUrl(ossUtil.generateUrl(material.getOssKey()))
                .title(material.getTitle())
                .description(material.getDescription())
                .subject(material.getSubject())
                .courseLevel(material.getCourseLevel())
                .documentType(material.getDocumentType())
                .keywords(material.getKeywords())
                .duration(material.getDuration())
                .language(material.getLanguage())
                .audioType(material.getAudioType())
                .speaker(material.getSpeaker())
                .transcriptionText(material.getTranscriptionText())
                .createdAt(material.getCreatedAt())
                .updatedAt(material.getUpdatedAt())
                .build();
    }
    
    private Long getMaterialCountByType(Integer userId, String materialType) {
        QueryWrapper<TeachingMaterial> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.eq("material_type", materialType);
        return teachingMaterialMapper.selectCount(query);
    }
    
    private List<Map<String, Object>> getSubjectStatistics(Integer userId) {
        // 这里可以通过自定义SQL查询来获取更详细的统计信息
        // 为了简化，暂时返回基本统计
        return Arrays.asList(
            Map.of("subject", "计算机科学", "count", getMaterialCountBySubject(userId, "计算机科学")),
            Map.of("subject", "数学", "count", getMaterialCountBySubject(userId, "数学")),
            Map.of("subject", "物理", "count", getMaterialCountBySubject(userId, "物理"))
        );
    }
    
    private Long getMaterialCountBySubject(Integer userId, String subject) {
        QueryWrapper<TeachingMaterial> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.eq("subject", subject);
        return teachingMaterialMapper.selectCount(query);
    }
}