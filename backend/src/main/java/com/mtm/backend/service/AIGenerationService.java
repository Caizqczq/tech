package com.mtm.backend.service;

import com.mtm.backend.model.DTO.ExplanationRequestDTO;
import com.mtm.backend.model.DTO.PPTGenerationDTO;
import com.mtm.backend.model.DTO.QuizGenerationDTO;
import com.mtm.backend.model.VO.TaskResponseVO;

/**
 * AI生成服务接口
 */
public interface AIGenerationService {
    
    /**
     * 生成教学解释内容
     */
    TaskResponseVO generateExplanation(ExplanationRequestDTO request, Integer userId);
    
    /**
     * 生成PPT课件
     */
    TaskResponseVO generatePPT(PPTGenerationDTO request, Integer userId);
    
    /**
     * 生成习题
     */
    TaskResponseVO generateQuiz(QuizGenerationDTO request, Integer userId);
}