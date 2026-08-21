package io.github.chenyouxin8.usercenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.chenyouxin8.usercenter.dto.ChangePasswordRequest;
import io.github.chenyouxin8.usercenter.dto.LoginRequest;
import io.github.chenyouxin8.usercenter.dto.LoginResponse;
import io.github.chenyouxin8.usercenter.dto.RegisterRequest;
import io.github.chenyouxin8.usercenter.dto.ResetPasswordRequest;
import io.github.chenyouxin8.usercenter.dto.UpdateUserRequest;
import io.github.chenyouxin8.usercenter.dto.UserResponse;
import io.github.chenyouxin8.usercenter.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==================== 无需认证的接口 ====================

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        UserResponse user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "code", 0, "message", "注册成功", "data", user));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        LoginResponse user = userService.login(request, ip);
        return ResponseEntity.ok()
                .body(Map.of("success", true, "code", 0, "message", "登录成功", "data", user));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        String newPassword = userService.resetPassword(request);
        return ResponseEntity.ok()
                .body(Map.of("success", true, "code", 0,
                        "message", "密码重置成功",
                        "data", Map.of("newPassword", newPassword)));
    }

    // ==================== 需要认证的接口 ====================

    /**
     * 获取当前登录用户信息（必须在 /{id} 之前，避免 "me" 被当作 id 解析）
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        UserResponse user = userService.getCurrentUser(currentUserId);
        return ResponseEntity.ok()
                .body(Map.of("success", true, "code", 0, "message", "查询成功", "data", user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            userService.logout(token);
        }
        return ResponseEntity.ok()
                .body(Map.of("success", true, "code", 0, "message", "登出成功"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok()
                .body(Map.of("success", true, "code", 0, "message", "查询成功", "data", user));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<UserResponse> page = userService.getUserList(pageNum, pageSize);
        return ResponseEntity.ok()
                .body(Map.of("success", true, "code", 0, "message", "查询成功", "data", page));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody UpdateUserRequest updateRequest) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        String ip = getClientIp(request);
        UserResponse user = userService.updateUser(currentUserId, id, updateRequest, ip);
        return ResponseEntity.ok()
                .body(Map.of("success", true, "code", 0, "message", "修改成功", "data", user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        String ip = getClientIp(request);
        userService.deleteUser(currentUserId, id, ip);
        return ResponseEntity.ok()
                .body(Map.of("success", true, "code", 0, "message", "删除成功"));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest changeRequest) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        String ip = getClientIp(request);
        userService.changePassword(currentUserId, id, changeRequest, ip);
        return ResponseEntity.ok()
                .body(Map.of("success", true, "code", 0, "message", "密码修改成功"));
    }

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            HttpServletRequest request,
            @RequestPart("file") MultipartFile file) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        String avatarUrl = userService.uploadAvatar(currentUserId, file);
        return ResponseEntity.ok()
                .body(Map.of("success", true, "code", 0, "message", "头像上传成功", "data", Map.of("avatarUrl", avatarUrl)));
    }

    @PostMapping("/email/verify/send")
    public ResponseEntity<Map<String, Object>> sendEmailVerification(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        String code = userService.sendEmailVerification(currentUserId);
        // TODO: 生产环境不应返回验证码，应通过邮件发送
        return ResponseEntity.ok()
                .body(Map.of("success", true, "code", 0,
                        "message", "验证码已发送",
                        "data", Map.of("verificationCode", code)));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Map<String, Object>> verifyEmail(
            HttpServletRequest request,
            @RequestBody Map<String, String> body) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        String verificationCode = body.get("code");
        userService.verifyEmail(currentUserId, verificationCode);
        return ResponseEntity.ok()
                .body(Map.of("success", true, "code", 0, "message", "邮箱验证成功"));
    }

    // ==================== 工具方法 ====================

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
