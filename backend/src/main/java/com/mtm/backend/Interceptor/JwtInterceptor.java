package com.mtm.backend.Interceptor;

import com.mtm.backend.utils.JwtUtil;
import com.mtm.backend.utils.ThreadLocalUtil;
import com.mtm.backend.utils.TokenBlacklist;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跳过 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if(token == null || tokenBlacklist.contains(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is missing or blacklisted");
            return false;
        }
        if(!jwtUtil.validateToken(token)){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalid");
            return false;
        }

        Integer userId = jwtUtil.getUserIdFromToken(token);
        ThreadLocalUtil.set(userId);
        return true;
    }
}
