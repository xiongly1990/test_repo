package com.parentkid.checkin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parentkid.checkin.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
