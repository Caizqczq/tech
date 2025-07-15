package com.mtm.backend.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtm.backend.repository.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库数据访问层
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {
}
