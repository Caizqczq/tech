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

    /** 1.1 用户注册 */
    @PostMapping("register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO registerDto) {
        if (registerDto.getUsername() == null || registerDto.getEmail() == null || registerDto.getPassword() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", new Date().toString());
            error.put("status", 400);
            error.put("error", "Bad Request");
            error.put("message", "用户名、邮箱和密码不能为空");
            error.put("path", "/api/auth/register");
            return ResponseEntity.badRequest().body(error);
        }
        RegisterVO registerVO = userService.registerUser(registerDto);
        return ResponseEntity.ok(registerVO);
    }

    /** 1.2 用户登录 */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        log.info("Login request received for email: {}", loginDTO.getEmail());
        
        if (loginDTO.getEmail() == null || loginDTO.getPassword() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", new Date().toString());
            error.put("status", 400);
            error.put("error", "Bad Request");
            error.put("message", "邮箱和密码不能为空");
            error.put("path", "/api/auth/login");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            LoginVO loginVO = userService.loginUser(loginDTO);
            if (loginVO == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("timestamp", new Date().toString());
                error.put("status", 401);
                error.put("error", "Unauthorized");
                error.put("message", "邮箱或密码错误");
                error.put("path", "/api/auth/login");
                return ResponseEntity.status(401).body(error);
            }
            
            String token = jwtUtil.generateToken(loginVO.getId(), loginVO.getUsername());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", loginVO);

            log.info("Login successful for user: {}", loginVO.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Login error: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", new Date().toString());
            error.put("status", 500);
            error.put("error", "Internal Server Error");
            error.put("message", "登录过程中发生错误");
            error.put("path", "/api/auth/login");
            return ResponseEntity.status(500).body(error);
        }
    }

    /** 1.3 用户登出 */
    @PostMapping("logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
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

    /** 1.4 获取当前用户信息 */
    @GetMapping("me")
    public ResponseEntity<?> getInfo(){
        Integer userId = ThreadLocalUtil.get();
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", new Date().toString());
            error.put("status", 401);
            error.put("error", "Unauthorized");
            error.put("message", "Token无效或已过期");
            error.put("path", "/api/auth/me");
            return ResponseEntity.status(401).body(error);
        }
        UserInfoVO userInfoVO = userService.getUserInfo(userId);
        if (userInfoVO == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", new Date().toString());
            error.put("status", 404);
            error.put("error", "Not Found");
            error.put("message", "用户不存在");
            error.put("path", "/api/auth/me");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(userInfoVO);
    }

    /** 1.5 测试接口 */
    @GetMapping("/hello")
    public String hello(HttpServletRequest request) {
        String username = jwtUtil.getUserFromToken(request.getHeader("Authorization"));
        return "Hello " + username + "!";
    }





}
