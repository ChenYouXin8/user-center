package io.github.chenyouxin8.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.chenyouxin8.usercenter.dto.ChangePasswordRequest;
import io.github.chenyouxin8.usercenter.dto.LoginRequest;
import io.github.chenyouxin8.usercenter.dto.LoginResponse;
import io.github.chenyouxin8.usercenter.dto.RegisterRequest;
import io.github.chenyouxin8.usercenter.dto.ResetPasswordRequest;
import io.github.chenyouxin8.usercenter.dto.UpdateUserRequest;
import io.github.chenyouxin8.usercenter.dto.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request, String ip);

    void logout(String token);

    UserResponse getUserById(Long id);

    UserResponse getCurrentUser(Long currentUserId);

    Page<UserResponse> getUserList(int pageNum, int pageSize);

    UserResponse updateUser(Long currentUserId, Long id, UpdateUserRequest request, String ip);

    void deleteUser(Long currentUserId, Long id, String ip);

    void changePassword(Long currentUserId, Long id, ChangePasswordRequest request, String ip);

    String resetPassword(ResetPasswordRequest request);

    String uploadAvatar(Long currentUserId, MultipartFile file);

    void verifyEmail(Long currentUserId, String code);

    String sendEmailVerification(Long currentUserId);
}
