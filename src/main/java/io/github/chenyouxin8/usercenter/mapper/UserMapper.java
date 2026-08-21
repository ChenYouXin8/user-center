package io.github.chenyouxin8.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.chenyouxin8.usercenter.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT id, username, email, avatarUrl, password FROM `user` WHERE username = #{username} AND isDelete = 0")
    List<User> selectByUsername(String username);

    @Select("SELECT COUNT(*) FROM `user` WHERE username = #{username} AND isDelete = 0")
    int countByUsername(String username);

    @Select("SELECT COUNT(*) FROM `user` WHERE email = #{email} AND isDelete = 0")
    int countByEmail(String email);

    @Select("SELECT id, username, email, avatarUrl, password FROM `user` WHERE email = #{email} AND isDelete = 0")
    User selectByEmail(String email);
}
