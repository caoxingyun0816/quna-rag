package com.quna.rag.service;

import com.quna.rag.entity.User;
import com.quna.rag.mapper.UserMapper;
import com.quna.rag.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public UserService(UserMapper userMapper, BCryptPasswordEncoder encoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null || !encoder.matches(password, user.getPassword())) {
            return Map.of("code", 401, "msg", "账号密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return Map.of("code", 200, "token", token, "role", user.getRole());
    }

    public Map<String, Object> register(String username, String password) {
        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            return Map.of("code", 400, "msg", "账号长度至少3个字符");
        }
        if (password == null || password.length() < 6) {
            return Map.of("code", 400, "msg", "密码长度至少6个字符");
        }

        // 检查用户名是否已存在
        User exist = userMapper.selectByUsername(username);
        if (exist != null) {
            return Map.of("code", 400, "msg", "用户名已存在");
        }

        // 插入新用户
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(encoder.encode(password));
        newUser.setRole("user");

        int result = userMapper.insert(newUser);
        if (result > 0) {
            return Map.of("code", 200, "msg", "注册成功，请登录");
        }
        return Map.of("code", 500, "msg", "注册失败，请重试");
    }
}