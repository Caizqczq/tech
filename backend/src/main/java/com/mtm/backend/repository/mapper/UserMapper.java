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

    /**
     * 插入用户记录
     */
    int insert(User record);

    /**
     * 根据主键查询用户
     */
    User selectByPrimaryKey(Integer id);

    /**
     * 根据标识符（邮箱或用户名）查找用户
     */
    User findByIdentifier(String identifier);

}
