package io.github.chenyouxin8.usercenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String operation;

    private String detail;

    private String ip;

    private LocalDateTime createTime;
}
