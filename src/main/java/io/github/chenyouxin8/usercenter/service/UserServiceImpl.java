package io.github.chenyouxin8.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.chenyouxin8.usercenter.dto.ChangePasswordRequest;
import io.github.chenyouxin8.usercenter.dto.LoginRequest;
import io.github.chenyouxin8.usercenter.dto.LoginResponse;
import io.github.chenyouxin8.usercenter.dto.RegisterRequest;
import io.github.chenyouxin8.usercenter.dto.ResetPasswordRequest;
import io.github.chenyouxin8.usercenter.dto.UpdateUserRequest;
import io.github.chenyouxin8.usercenter.dto.UserResponse;
import io.github.chenyouxin8.usercenter.entity.User;
import io.github.chenyouxin8.usercenter.exception.BusinessException;
import io.github.chenyouxin8.usercenter.mapper.UserMapper;
import io.github.chenyouxin8.usercenter.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
    private final UserMapper userMapper;
    private final PasswordHasher passwordHasher;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;
    private final OperationLogService operationLogService;
    private final EmailVerificationService emailVerificationService;

    @Value("${file.upload-dir:uploads/avatars}")
    private String uploadDir;

    public UserServiceImpl(UserMapper userMapper,
                           PasswordHasher passwordHasher,
                           JwtUtil jwtUtil,
                           LoginAttemptService loginAttemptService,
                           TokenBlacklistService tokenBlacklistService,
                           OperationLogService operationLogService,
                           EmailVerificationService emailVerificationService) {
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
        this.jwtUtil = jwtUtil;
        this.loginAttemptService = loginAttemptService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.operationLogService = operationLogService;
        this.emailVerificationService = emailVerificationService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        validate(request);
        String username = request.username().trim();
        String email = request.email() != null ? request.email().trim().toLowerCase(Locale.ROOT) : null;

        // 用户名唯一性校验
        int usernameCount = userMapper.countByUsername(username);
        if (usernameCount > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名已存在");
        }

        if (email != null) {
            int count = userMapper.countByEmail(email);
            if (count > 0) {
                throw new BusinessException(HttpStatus.CONFLICT, "邮箱已注册");
            }
        }

        String encodedPassword = passwordHasher.hash(request.password());
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setIsValid(1);
        try {
            userMapper.insert(user);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名或邮箱已存在");
        }

        if (user.getId() == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "注册失败，未生成用户编号");
        }
        return toUserResponse(user);
    }

    private void validate(RegisterRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请求体不能为空");
        }
        if (request.username() == null || request.username().isBlank() || request.username().trim().length() > 64) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名不能为空且长度不能超过 64 个字符");
        }
        if (request.email() == null || !EMAIL_PATTERN.matcher(request.email().trim()).matches()
                || request.email().trim().length() > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "邮箱格式不正确");
        }
        if (request.password() == null || request.password().length() < 8 || request.password().length() > 72) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密码长度必须是 8 到 72 位");
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "两次输入的密码不一致");
        }
    }

    @Override
    public LoginResponse login(LoginRequest request, String ip) {
        validateLogin(request);
        String username = request.username().trim();

        // 检查账号是否被锁定
        if (loginAttemptService.isBlocked(username)) {
            long remaining = loginAttemptService.getRemainingLockSeconds(username);
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS,
                    "登录失败次数过多，账号已锁定 " + remaining + " 秒");
        }

        List<User> users = userMapper.selectByUsername(username);

        if (users.isEmpty()) {
            loginAttemptService.loginFailed(username);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        User user = users.get(0);

        if (!passwordHasher.matches(request.password(), user.getPassword())) {
            loginAttemptService.loginFailed(username);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        // 登录成功，清除失败记录
        loginAttemptService.loginSucceeded(username);

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), username);

        // 记录操作日志
        operationLogService.log(user.getId(), username, "LOGIN", "用户登录", ip);

        return new LoginResponse(user.getId(), username, user.getEmail(), user.getAvatarUrl(), token);
    }

    private void validateLogin(LoginRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请求体不能为空");
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名不能为空");
        }
        if (request.password() == null || request.password().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密码不能为空");
        }
    }

    @Override
    public void logout(String token) {
        long expiry = jwtUtil.getExpiryTimestamp(token);
        tokenBlacklistService.blacklist(token, expiry);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDelete() == 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        return toUserResponse(user);
    }

    @Override
    public UserResponse getCurrentUser(Long currentUserId) {
        User user = userMapper.selectById(currentUserId);
        if (user == null || user.getIsDelete() == 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        return toUserResponse(user);
    }

    @Override
    public Page<UserResponse> getUserList(int pageNum, int pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsDelete, 0);
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> userPage = userMapper.selectPage(page, wrapper);

        Page<UserResponse> responsePage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserResponse> records = userPage.getRecords().stream()
                .map(this::toUserResponse)
                .toList();
        responsePage.setRecords(records);
        return responsePage;
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long currentUserId, Long id, UpdateUserRequest request, String ip) {
        if (!currentUserId.equals(id)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权修改其他用户信息");
        }

        User user = userMapper.selectById(id);
        if (user == null || user.getIsDelete() == 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }

        if (request.username() != null && !request.username().isBlank()) {
            String newUsername = request.username().trim();
            // 检查新用户名是否被占用
            int count = userMapper.countByUsername(newUsername);
            if (count > 0 && !newUsername.equals(user.getUsername())) {
                throw new BusinessException(HttpStatus.CONFLICT, "用户名已存在");
            }
            user.setUsername(newUsername);
        }
        if (request.email() != null && !request.email().isBlank()) {
            String email = request.email().trim().toLowerCase(Locale.ROOT);
            User existing = userMapper.selectByEmail(email);
            if (existing != null && !existing.getId().equals(id)) {
                throw new BusinessException(HttpStatus.CONFLICT, "邮箱已被其他用户使用");
            }
            user.setEmail(email);
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }

        userMapper.updateById(user);
        operationLogService.log(currentUserId, user.getUsername(), "UPDATE_USER", "修改用户信息", ip);
        return toUserResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long currentUserId, Long id, String ip) {
        if (!currentUserId.equals(id)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权删除其他用户");
        }

        User user = userMapper.selectById(id);
        if (user == null || user.getIsDelete() == 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        user.setIsDelete(1);
        userMapper.updateById(user);
        operationLogService.log(currentUserId, user.getUsername(), "DELETE_USER", "注销账号", ip);
    }

    @Override
    @Transactional
    public void changePassword(Long currentUserId, Long id, ChangePasswordRequest request, String ip) {
        if (!currentUserId.equals(id)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权修改其他用户密码");
        }

        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请求体不能为空");
        }
        if (request.oldPassword() == null || request.oldPassword().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "旧密码不能为空");
        }
        if (request.newPassword() == null || request.newPassword().length() < 8 || request.newPassword().length() > 72) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "新密码长度必须是 8 到 72 位");
        }
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "两次输入的新密码不一致");
        }

        User user = userMapper.selectById(id);
        if (user == null || user.getIsDelete() == 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }

        if (!passwordHasher.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "旧密码错误");
        }

        user.setPassword(passwordHasher.hash(request.newPassword()));
        userMapper.updateById(user);
        operationLogService.log(currentUserId, user.getUsername(), "CHANGE_PASSWORD", "修改密码", ip);
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "邮箱不能为空");
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "该邮箱未注册");
        }

        String newPassword = generateRandomPassword();
        user.setPassword(passwordHasher.hash(newPassword));
        userMapper.updateById(user);

        // TODO: 实际项目中应通过邮件发送新密码
        return newPassword;
    }

    @Override
    @Transactional
    public String uploadAvatar(Long currentUserId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请选择要上传的文件");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "文件大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "只能上传图片文件");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;

        Path uploadPath = Paths.get(uploadDir);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            User user = userMapper.selectById(currentUserId);
            if (user == null || user.getIsDelete() == 1) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
            }
            String avatarUrl = "/api/files/avatars/" + newFilename;
            user.setAvatarUrl(avatarUrl);
            userMapper.updateById(user);

            return avatarUrl;
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String sendEmailVerification(Long currentUserId) {
        User user = userMapper.selectById(currentUserId);
        if (user == null || user.getIsDelete() == 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请先设置邮箱");
        }
        String code = emailVerificationService.generateCode(currentUserId);
        // TODO: 实际项目中应通过邮件发送验证码
        return code;
    }

    @Override
    @Transactional
    public void verifyEmail(Long currentUserId, String code) {
        emailVerificationService.verify(currentUserId, code);

        User user = userMapper.selectById(currentUserId);
        if (user == null || user.getIsDelete() == 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        // isValid = 1 表示邮箱已验证
        user.setIsValid(1);
        userMapper.updateById(user);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getGender(),
                user.getPhone(),
                user.getIsValid(),
                user.getCreateTime()
        );
    }
}
