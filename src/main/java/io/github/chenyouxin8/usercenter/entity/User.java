package io.github.chenyouxin8.usercenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String avatarUrl;

    private Integer gender;

    private String password;

    private String phone;

    private String email;

    private Integer isValid;

    private Integer userRole;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDelete;
}
