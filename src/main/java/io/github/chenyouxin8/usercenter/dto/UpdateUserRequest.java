package io.github.chenyouxin8.usercenter.dto;

public record UpdateUserRequest(
        String username,
        String email,
        String avatarUrl,
        Integer gender,
        String phone
) {
}
