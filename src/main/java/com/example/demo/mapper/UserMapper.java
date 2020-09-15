package com.example.demo.mapper;

import com.example.demo.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("select * from sys_user where username = #{username}")
    SysUser getByUsername(String username);
}
