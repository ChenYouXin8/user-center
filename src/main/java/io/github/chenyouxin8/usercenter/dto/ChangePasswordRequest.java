package io.github.chenyouxin8.usercenter.dto;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword,
        String confirmNewPassword
) {
}
