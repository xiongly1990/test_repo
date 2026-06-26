package com.parentkid.checkin.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parentkid.checkin.dto.TaskDTO;
import com.parentkid.checkin.entity.Task;
import com.parentkid.checkin.mapper.TaskMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public List<Task> getByChildId(Long childId) {
        String cacheKey = "task:list:" + childId;
        Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
        if (cachedObj != null) {
            try {
                String json = objectMapper.writeValueAsString(cachedObj);
                return objectMapper.readValue(json, new TypeReference<List<Task>>() {});
            } catch (Exception e) {
                return null;
            }
        }
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getChildId, childId)
                .eq(Task::getStatus, 1)
                .orderByAsc(Task::getSort);
        List<Task> list = taskMapper.selectList(wrapper);
        redisTemplate.opsForValue().set(cacheKey, list, 30, TimeUnit.MINUTES);
        return list;
    }

    public Task getById(Long id) {
        return taskMapper.selectById(id);
    }

    public List<Task> getTodayTasks(Long childId) {
        int dayOfWeek = DateUtil.dayOfWeek(DateUtil.date()) - 1;
        List<Task> allTasks = getByChildId(childId);
        if (allTasks == null) {
            return List.of();
        }
        return allTasks.stream().filter(task -> {
            if (task.getCycleType() == 1) {
                return true;
            } else if (task.getCycleType() == 2) {
                return dayOfWeek >= 1 && dayOfWeek <= 5;
            } else if (task.getCycleType() == 3) {
                return dayOfWeek == 0 || dayOfWeek == 6;
            }
            return false;
        }).toList();
    }

    public void add(TaskDTO dto) {
        Task task = new Task();
        BeanUtils.copyProperties(dto, task);
        if (task.getStatus() == null) {
            task.setStatus(1);
        }
        if (task.getNeedPhoto() == null) {
            task.setNeedPhoto(1);
        }
        if (task.getSort() == null) {
            task.setSort(0);
        }
        taskMapper.insert(task);
        clearCache(dto.getChildId());
    }

    public void update(TaskDTO dto) {
        Task task = new Task();
        BeanUtils.copyProperties(dto, task);
        taskMapper.updateById(task);
        clearCache(dto.getChildId());
    }

    public void delete(Long id) {
        Task task = taskMapper.selectById(id);
        if (task != null) {
            taskMapper.deleteById(id);
            clearCache(task.getChildId());
        }
    }

    private void clearCache(Long childId) {
        redisTemplate.delete("task:list:" + childId);
    }
}