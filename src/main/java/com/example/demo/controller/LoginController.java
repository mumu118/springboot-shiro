package com.example.demo.controller;

import cn.hutool.core.util.StrUtil;
import com.example.demo.common.Result;
import com.example.demo.domain.SysUser;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/sys")
@Slf4j
public class LoginController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result login(@RequestBody SysUser sysUser){

        String username = sysUser.getUsername();
        String password = sysUser.getPassword();

        //1. 校验用户是否有效
        SysUser loginUser = userService.getByUsername(username);
        if (loginUser == null){
            throw new RuntimeException("用户不存在");
        }
        if (!loginUser.getPassword().equals(password)){
            throw new RuntimeException("密码错误");
        }

        // 生成token
        String token = JwtUtil.sign(username, password);
        redisUtil.set(username+":token", token);
        // 设置超时时间
        redisUtil.expire(username+":token", JwtUtil.EXPIRE_TIME / 1000);

        return Result.success("登录成功",token);
    }

    /**
     * 退出登录
     *
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/logout")
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        //用户退出逻辑
        String token = request.getHeader("token");
        if (StrUtil.isEmpty(token)) {
            return Result.failure("登出失败");
        }
        String username = JwtUtil.getUsername(token);
        SysUser sysUser = userService.getByUsername(username);
        if (sysUser != null) {
            log.info(" 用户名:  " + sysUser.getUsername() + ",退出成功！ ");
            // 删除缓存
            redisUtil.del(username+":token");
            return Result.failure("退出登录成功！");
        } else {
            return Result.failure("无效的token");
        }
    }
}
