package com.quna.rag.mapper;

import com.quna.rag.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface UserMapper {
    @Select("SELECT * FROM user WHERE username=#{username}")
    User selectByUsername(String username);

    @Insert("INSERT INTO user(username, password, role) VALUES(#{username}, #{password}, 'user')")
    int insert(User user);
}