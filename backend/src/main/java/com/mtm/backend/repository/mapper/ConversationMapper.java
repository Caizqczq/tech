package com.mtm.backend.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtm.backend.repository.Conversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}