package com.mtm.backend.service;

import com.mtm.backend.model.DTO.LoginDTO;
import com.mtm.backend.model.DTO.RegisterDTO;
import com.mtm.backend.model.VO.LoginVO;
import com.mtm.backend.model.VO.RegisterVO;
import com.mtm.backend.model.VO.UserInfoVO;
import com.mtm.backend.repository.User;
import com.mtm.backend.repository.mapper.UserMapper;
import com.mtm.backend.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    /***
     * 注册新账户
     * @param registerDto
     */
    public RegisterVO registerUser(RegisterDTO registerDto) {
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPasswordHash(PasswordUtil.hashPassword(registerDto.getPassword()));
        user.setEmail(registerDto.getEmail());
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        userMapper.insert(user);

        RegisterVO registerVO = new RegisterVO();
        registerVO.setId(user.getId());
        registerVO.setUsername(user.getUsername());
        registerVO.setEmail(user.getEmail());
        registerVO.setCreatedAt(user.getCreatedAt());
        return registerVO;
    }


    public LoginVO loginUser(LoginDTO loginDTO) {
        User user=userMapper.findByIdentifier(loginDTO.getEmail());
        if (user == null || !PasswordUtil.verifyPassword(loginDTO.getPassword(), user.getPasswordHash())) {
            return null; // 登录失败
        }
        LoginVO loginVO = new LoginVO();
        loginVO.setId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setEmail(user.getEmail());
        loginVO.setAvatar(user.getAvatar());
        return loginVO;
    }

    public UserInfoVO getUserInfo(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO,"passwordHash", "updatedAt");
        return userInfoVO;
    }
}
