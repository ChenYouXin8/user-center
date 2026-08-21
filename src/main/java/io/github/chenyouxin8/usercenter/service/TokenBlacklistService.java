package io.github.chenyouxin8.usercenter.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Token 黑名单服务（内存级别）
 * 用于登出后将 Token 加入黑名单，使其失效。
 * 生产环境应使用 Redis 实现分布式共享。
 */
@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * 将 Token 加入黑名单
     * @param token JWT token
     * @param expiryTimestamp Token 过期时间戳（用于定期清理）
     */
    public void blacklist(String token, long expiryTimestamp) {
        blacklist.put(token, expiryTimestamp);
        cleanExpired();
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    /**
     * 清理已过期的黑名单条目，避免内存泄漏
     */
    private void cleanExpired() {
        long now = System.currentTimeMillis() / 1000;
        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
