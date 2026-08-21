package io.github.chenyouxin8.usercenter.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登录失败次数限制服务（内存级别）
 * 同一用户名连续失败 5 次后锁定 15 分钟
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000; // 15 minutes

    private final ConcurrentHashMap<String, AtomicInteger> attemptCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lockTimes = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        Long lockTime = lockTimes.get(username);
        if (lockTime == null) {
            return false;
        }
        if (System.currentTimeMillis() - lockTime > LOCK_DURATION_MS) {
            // 锁定已过期，清除记录
            attemptCounts.remove(username);
            lockTimes.remove(username);
            return false;
        }
        return true;
    }

    public void loginFailed(String username) {
        AtomicInteger attempts = attemptCounts.computeIfAbsent(username, k -> new AtomicInteger(0));
        int count = attempts.incrementAndGet();
        if (count >= MAX_ATTEMPTS) {
            lockTimes.put(username, System.currentTimeMillis());
        }
    }

    public void loginSucceeded(String username) {
        attemptCounts.remove(username);
        lockTimes.remove(username);
    }

    public long getRemainingLockSeconds(String username) {
        Long lockTime = lockTimes.get(username);
        if (lockTime == null) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - lockTime;
        long remaining = LOCK_DURATION_MS - elapsed;
        return remaining > 0 ? (remaining + 999) / 1000 : 0;
    }
}
