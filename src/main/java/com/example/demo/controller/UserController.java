package com.example.demo.controller;

import com.example.demo.common.Result;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 需要认证授权，user:list权限
     */
    @RequiresPermissions("user:list")
    @RequestMapping("list")
    public Result userList() {
        return Result.success("获取用户列表成功","list");
    }

    /**
     * filterChainDefinitionMap.put("/sys/login", "anon"); 可以直接访问
     */
    @RequestMapping("test")
    public String test() {
        return "不用登陆直接访问的接口";
    }
}
