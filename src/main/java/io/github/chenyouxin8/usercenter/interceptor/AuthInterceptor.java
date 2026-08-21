package io.github.chenyouxin8.usercenter.interceptor;

import io.github.chenyouxin8.usercenter.service.TokenBlacklistService;
import io.github.chenyouxin8.usercenter.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthInterceptor(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"\u672a\u767b\u5f55\u6216Token\u5df2\u8fc7\u671f\"}");
            return false;
        }

        String token = authHeader.substring(7);

        // 检查 Token 是否在黑名单中（已登出）
        if (tokenBlacklistService.isBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"Token\u5df2\u5931\u6548\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55\"}");
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"Token\u65e0\u6548\u6216\u5df2\u8fc7\u671f\"}");
            return false;
        }

        // 将用户信息存入 request attribute
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUsername", username);
        return true;
    }
}
