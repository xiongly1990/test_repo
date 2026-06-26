package com.parentkid.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parentkid.checkin.dto.ChildDTO;
import com.parentkid.checkin.entity.Child;
import com.parentkid.checkin.mapper.ChildMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChildService {

    @Autowired
    private ChildMapper childMapper;

    public Page<Child> getPage(int pageNum, int pageSize, String keyword) {
        Page<Child> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Child> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Child::getName, keyword);
        }
        wrapper.orderByDesc(Child::getCreateTime);
        return childMapper.selectPage(page, wrapper);
    }

    public Child getById(Long id) {
        return childMapper.selectById(id);
    }

    public void add(ChildDTO dto) {
        Child child = new Child();
        BeanUtils.copyProperties(dto, child);
        child.setPoints(0);
        child.setStreakDays(0);
        child.setTotalDays(0);
        childMapper.insert(child);
    }

    public void update(ChildDTO dto) {
        Child child = new Child();
        BeanUtils.copyProperties(dto, child);
        childMapper.updateById(child);
    }

    public void delete(Long id) {
        childMapper.deleteById(id);
    }
}
