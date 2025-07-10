package com.mtm.backend.controller;

import com.mtm.backend.model.DTO.LoginDTO;
import com.mtm.backend.model.DTO.RegisterDTO;
import com.mtm.backend.model.VO.LoginVO;
import com.mtm.backend.model.VO.RegisterVO;
import com.mtm.backend.model.VO.UserInfoVO;
import com.mtm.backend.repository.User;
import com.mtm.backend.service.UserService;
import com.mtm.backend.utils.JwtUtil;
import com.mtm.backend.utils.ThreadLocalUtil;
import com.mtm.backend.utils.TokenBlacklist;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    private final JwtUtil jwtUtil;

    private final TokenBlacklist tokenBlacklist;

    /**
     * 用户注册
     * @param registerDto
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<RegisterVO> register(@RequestBody RegisterDTO registerDto) {
        if (registerDto.getUsername() == null || registerDto.getEmail() == null || registerDto.getPassword() == null) {
            return ResponseEntity.badRequest().build();
        }
        RegisterVO registerVO = userService.registerUser(registerDto);
        return ResponseEntity.ok(registerVO);
    }

    /**
     * 用户登录
     * @param loginDTO
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody LoginDTO loginDTO) {
        if (loginDTO.getEmail() == null || loginDTO.getPassword() == null) {
            return ResponseEntity.badRequest().build();
        }
        LoginVO loginVO = userService.loginUser(loginDTO);
        if (loginVO == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        String token = jwtUtil.generateToken(loginVO.getId(), loginVO.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user",loginVO);

        return ResponseEntity.ok(response);
    }

    /**
     * 测试接口，验证JWT拦截器是否工作正常
     * @param request
     * @return
     */
    @GetMapping("/hello")
    public String hello(HttpServletRequest request) {
        String username = jwtUtil.getUserFromToken(request.getHeader("Authorization"));
        return "Hello " + username + "!";
    }

    /**
     * 用户登出
     * @param request
     * @return
     */
    @PostMapping("logout")
    public ResponseEntity<Void> logout(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token != null) {
            Date exp=jwtUtil.getExpirationDateFromToken(token);
            long ttlMillis = exp.getTime() - System.currentTimeMillis();
            tokenBlacklist.add(token,ttlMillis);
            log.info("User logged out, token invalidated: {}", token);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping("me")
    public ResponseEntity<UserInfoVO> getInfo(){
        Integer userId = ThreadLocalUtil.get();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        UserInfoVO userInfoVO = userService.getUserInfo(userId);
        if (userInfoVO == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(userInfoVO);
    }



}
