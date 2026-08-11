package com.pureblog.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.auth.entity.LoginLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLogDO> {
}
