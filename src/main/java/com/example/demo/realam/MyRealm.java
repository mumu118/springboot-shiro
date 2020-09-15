package com.example.demo.realam;

import com.example.demo.domain.JwtToken;
import com.example.demo.domain.SysUser;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class MyRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean supports(AuthenticationToken token) {
        // 定义该Realm可以处理哪个类型的token
        return token instanceof JwtToken;
    }

    /**
     * 用户认证成功后授权处理 获取用户权限信息，包括角色以及权限。
     * @param principals token
     * @return AuthorizationInfo 权限信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        log.info("===========用户授权=========");

        SysUser sysUser = null;
        String userName = null;
        if (principals != null) {
            sysUser = (SysUser) principals.getPrimaryPrincipal();
            userName = sysUser.getUsername();
        }
        SysUser dbUser = userService.getByUsername(userName);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        if (dbUser != null){
            // 设置用户角色集合，比如admin
            Set<String> roles = new HashSet<>();
            roles.add("admin");
            info.setRoles(roles);

            // 设置用户拥有的权限集合，比如“sys:role:add,sys:user:add”
            Set<String> permissionSet = new HashSet<>();
            permissionSet.add("user:list");
            permissionSet.add("user:del");
            info.addStringPermissions(permissionSet);
            // to do 用户权限缓存处理
        }
        return info;
    }

    /**
     * 身份认证，主要根据token认证用户身份
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {

        log.info("==============用户身份认证===============");

        String token = (String) auth.getCredentials();
        if (token == null) {
            throw new AuthenticationException("token无效!");
        }

        // 校验token有效性
        // 解密获得username，用于和数据库进行对比
        String username = JwtUtil.getUsername(token);

        // 校验token是否超时失效或者账号密码是否错误
        String cacheToken = (String) redisUtil.get(username+":token");
        if (!token.equals(cacheToken) || username == null) {
            throw new AuthenticationException("token非法无效!");
        }
        // 查询用户信息
        // to do 用户信息缓存处理 token刷新
        SysUser loginUser = new SysUser();
        SysUser sysUser = userService.getByUsername(username);
        if (sysUser == null) {
            throw new AuthenticationException("token非法无效!");
        }

        BeanUtils.copyProperties(sysUser, loginUser);

        return new SimpleAuthenticationInfo(loginUser, token, getName());
    }
}
