package com.example.demo.filter;

import com.alibaba.fastjson.JSON;
import com.example.demo.common.HttpContextUtils;
import com.example.demo.common.Result;
import com.example.demo.domain.JwtToken;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 执行顺序 isAccessAllowed ->onAccessDenied
 */
public class OAuth2Filter extends AuthenticatingFilter {

    /**
     * createToken 创建登录的身份token
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        //获取请求token
        String token = getRequestToken((HttpServletRequest) request);

        if(StringUtils.isBlank(token)){
            return null;
        }
        return new JwtToken(token);
    }

    /**
     * isAccessAllowed 是否允许被访问
     */
    @SneakyThrows
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 对于OPTION请求做拦截，不做token校验
        return ((HttpServletRequest) request).getMethod().equals(RequestMethod.OPTIONS.name());
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        //获取请求token，如果token不存在，直接返回401
        String token = getRequestToken((HttpServletRequest) request);
        if(StringUtils.isBlank(token)){
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Allow-Origin", HttpContextUtils.getOrigin());

            String json = JSON.toJSONString(new Result<>().error(401));

            httpResponse.getWriter().print(json);

            return false;
        }

        // token存在，执行自定义realm中的认证授权
        return executeLogin(request, response);
    }

    /**
     * 自定义realm中doGetAuthenticationInfo出现异常时（用户认证失败）执行
     * 参数AuthenticationException e就是自定义realm中doGetAuthenticationInfo出现异常时抛出的异常信息
     **/
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setContentType("application/json;charset=utf-8");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Origin", HttpContextUtils.getOrigin());
        try {
            //处理认证失败的异常
            Throwable throwable = e.getCause() == null ? e : e.getCause();
            // 认证失败返回认证失败信息
            // code401为魔法值，应该将常用的状态码定义为枚举使用
            Result r = new Result().error(401, throwable.getMessage());

            // 将返回结果对象转为json串返回
            String json = JSON.toJSONString(r);
            httpResponse.getWriter().print(json);
        } catch (IOException e1) {

        }
        return false;
    }

    /**
     * 获取请求的token
     */
    private String getRequestToken(HttpServletRequest httpRequest){
        //从header中获取token
        String token = httpRequest.getHeader("token");

        //如果header中不存在token，则从参数中获取token
        if(StringUtils.isBlank(token)){
            token = httpRequest.getParameter("token");
        }
        return token;
    }


}