package com.parentkid.checkin.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parentkid.checkin.entity.Admin;
import com.parentkid.checkin.mapper.AdminMapper;
import com.parentkid.checkin.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Map<String, Object> login(String username, String password) {
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username)
        );

        if (admin == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!BCrypt.checkpw(password, admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername());
        redisTemplate.opsForValue().set("admin:token:" + admin.getId(), token, 24, TimeUnit.HOURS);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", admin.getId());
        userInfo.put("username", admin.getUsername());
        userInfo.put("nickname", admin.getNickname());
        userInfo.put("avatar", admin.getAvatar());
        result.put("userInfo", userInfo);

        return result;
    }

    public void logout(Long userId) {
        redisTemplate.delete("admin:token:" + userId);
    }

    public Admin getAdminInfo(Long userId) {
        return adminMapper.selectById(userId);
    }
}
