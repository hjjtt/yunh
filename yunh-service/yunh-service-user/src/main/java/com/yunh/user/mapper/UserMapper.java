package com.yunh.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunh.user.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
