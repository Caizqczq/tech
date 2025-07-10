package com/mtm/backend/repository.mapper;

import com/mtm/backend/repository.Users;

/**
* @author Scociz
* @description 针对表【users(用户表)】的数据库操作Mapper
* @createDate 2025-07-09 20:47:49
* @Entity com/mtm/backend/repository.Users
*/
public interface UsersMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Users record);

    int insertSelective(Users record);

    Users selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Users record);

    int updateByPrimaryKey(Users record);

}
