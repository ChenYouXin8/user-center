package io.github.chenyouxin8.usercenter.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String avatarUrl,
        Integer gender,
        String phone,
        Integer isValid,
        Integer userRole,
        LocalDateTime createTime
) {
}
