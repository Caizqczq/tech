package com.mtm.backend.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtm.backend.repository.KnowledgeBaseResource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库资源关联Mapper
 */
@Mapper
public interface KnowledgeBaseResourceMapper extends BaseMapper<KnowledgeBaseResource> {
}