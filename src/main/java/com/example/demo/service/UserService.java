package com.example.demo.service;

import com.example.demo.domain.SysUser;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public SysUser getByUsername(String username) {
        return userMapper.getByUsername(username);
    }
}
