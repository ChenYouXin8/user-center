package io.github.chenyouxin8.usercenter.dto;

public record LoginResponse(
        Long id,
        String username,
        String email,
        String avatarUrl,
        String token
) {
}
