package io.github.chenyouxin8.usercenter.exception;

import java.util.Map;

public record ErrorResponse(
        int code,
        String message,
        Map<String, String> errors
) {
}
