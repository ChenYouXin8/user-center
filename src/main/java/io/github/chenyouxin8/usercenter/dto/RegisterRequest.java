package io.github.chenyouxin8.usercenter.dto;

public record RegisterRequest(
        String username,
        String email,
        String password,
        String confirmPassword
) {
}
