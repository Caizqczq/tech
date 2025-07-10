package com.mtm.backend.repository.mapper;

import com.mtm.backend.repository.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Scociz
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2025-07-09 23:42:13
* @Entity com.mtm.backend.repository.User
*/
@Mapper
public interface UserMapper {

    int deleteByPrimaryKey(Long id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User findByIdentifier(String identifier);



}
