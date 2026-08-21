package io.github.chenyouxin8.usercenter.service;

import io.github.chenyouxin8.usercenter.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮箱验证码服务（内存级别）
 * 生成 6 位数字验证码，有效期 10 分钟
 * 生产环境应使用 Redis + 邮件服务
 */
@Service
public class EmailVerificationService {

    private static final long CODE_EXPIRY_MS = 10 * 60 * 1000; // 10 minutes
    private static final SecureRandom RANDOM = new SecureRandom();

    // key: userId, value: [code, expiryTimestamp]
    private final ConcurrentHashMap<Long, String[]> codes = new ConcurrentHashMap<>();

    /**
     * 生成并存储验证码
     * @return 验证码
     */
    public String generateCode(Long userId) {
        String code = String.format("%06d", RANDOM.nextInt(1000000));
        long expiry = System.currentTimeMillis() + CODE_EXPIRY_MS;
        codes.put(userId, new String[]{code, String.valueOf(expiry)});
        // TODO: 实际项目中应通过邮件发送验证码
        return code;
    }

    /**
     * 验证验证码
     */
    public void verify(Long userId, String code) {
        String[] stored = codes.get(userId);
        if (stored == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请先获取验证码");
        }

        long expiry = Long.parseLong(stored[1]);
        if (System.currentTimeMillis() > expiry) {
            codes.remove(userId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "验证码已过期");
        }

        if (!stored[0].equals(code)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "验证码错误");
        }

        // 验证成功，清除
        codes.remove(userId);
    }
}
