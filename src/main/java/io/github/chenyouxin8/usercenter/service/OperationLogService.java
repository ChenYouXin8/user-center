package io.github.chenyouxin8.usercenter.service;

import io.github.chenyouxin8.usercenter.entity.OperationLog;
import io.github.chenyouxin8.usercenter.mapper.OperationLogMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Async
    public void log(Long userId, String username, String operation, String detail, String ip) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperation(operation);
        log.setDetail(detail);
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    @Async
    public void log(String username, String operation, String detail, String ip) {
        log(null, username, operation, detail, ip);
    }
}
