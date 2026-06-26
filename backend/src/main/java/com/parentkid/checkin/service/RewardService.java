package com.parentkid.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parentkid.checkin.dto.RewardDTO;
import com.parentkid.checkin.entity.Reward;
import com.parentkid.checkin.entity.RewardCategory;
import com.parentkid.checkin.mapper.RewardCategoryMapper;
import com.parentkid.checkin.mapper.RewardMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RewardService {

    @Autowired
    private RewardMapper rewardMapper;

    @Autowired
    private RewardCategoryMapper rewardCategoryMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<RewardCategory> getCategories() {
        String cacheKey = "reward:categories";
        List<RewardCategory> cached = (List<RewardCategory>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        LambdaQueryWrapper<RewardCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(RewardCategory::getSort);
        List<RewardCategory> list = rewardCategoryMapper.selectList(wrapper);
        redisTemplate.opsForValue().set(cacheKey, list, 30, TimeUnit.MINUTES);
        return list;
    }

    public Page<Reward> getPage(int pageNum, int pageSize, Long categoryId, String keyword) {
        Page<Reward> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Reward> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Reward::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Reward::getName, keyword);
        }
        wrapper.orderByAsc(Reward::getSort);
        return rewardMapper.selectPage(page, wrapper);
    }

    public List<Reward> getByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Reward> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reward::getCategoryId, categoryId)
                .eq(Reward::getStatus, 1)
                .orderByAsc(Reward::getSort);
        return rewardMapper.selectList(wrapper);
    }

    public Reward getById(Long id) {
        return rewardMapper.selectById(id);
    }

    public void add(RewardDTO dto) {
        Reward reward = new Reward();
        BeanUtils.copyProperties(dto, reward);
        if (reward.getStatus() == null) {
            reward.setStatus(1);
        }
        if (reward.getStock() == null) {
            reward.setStock(-1);
        }
        if (reward.getSort() == null) {
            reward.setSort(0);
        }
        rewardMapper.insert(reward);
        clearCache();
    }

    public void update(RewardDTO dto) {
        Reward reward = new Reward();
        BeanUtils.copyProperties(dto, reward);
        rewardMapper.updateById(reward);
        clearCache();
    }

    public void delete(Long id) {
        rewardMapper.deleteById(id);
        clearCache();
    }

    private void clearCache() {
        redisTemplate.delete("reward:categories");
    }
}
