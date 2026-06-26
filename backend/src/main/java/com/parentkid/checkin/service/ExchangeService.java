package com.parentkid.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parentkid.checkin.entity.Child;
import com.parentkid.checkin.entity.ExchangeRecord;
import com.parentkid.checkin.entity.Reward;
import com.parentkid.checkin.mapper.ChildMapper;
import com.parentkid.checkin.mapper.ExchangeRecordMapper;
import com.parentkid.checkin.mapper.RewardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class ExchangeService {

    @Autowired
    private ExchangeRecordMapper exchangeRecordMapper;

    @Autowired
    private RewardMapper rewardMapper;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Page<ExchangeRecord> getRecords(int pageNum, int pageSize, Long childId, Integer status) {
        Page<ExchangeRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExchangeRecord> wrapper = new LambdaQueryWrapper<>();
        if (childId != null) {
            wrapper.eq(ExchangeRecord::getChildId, childId);
        }
        if (status != null) {
            wrapper.eq(ExchangeRecord::getStatus, status);
        }
        wrapper.orderByDesc(ExchangeRecord::getCreateTime);
        return exchangeRecordMapper.selectPage(page, wrapper);
    }

    @Transactional
    public void exchange(Long childId, Long rewardId) {
        Child child = childMapper.selectById(childId);
        if (child == null) {
            throw new RuntimeException("孩子不存在");
        }

        Reward reward = rewardMapper.selectById(rewardId);
        if (reward == null) {
            throw new RuntimeException("奖励不存在");
        }
        if (reward.getStatus() != 1) {
            throw new RuntimeException("奖励已下架");
        }
        if (reward.getStock() != null && reward.getStock() != -1 && reward.getStock() <= 0) {
            throw new RuntimeException("奖励库存不足");
        }
        if (child.getPoints() < reward.getPoints()) {
            throw new RuntimeException("积分不足");
        }

        String lockKey = "exchange:lock:" + childId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            throw new RuntimeException("操作太频繁，请稍后再试");
        }

        try {
            child.setPoints(child.getPoints() - reward.getPoints());
            childMapper.updateById(child);

            if (reward.getStock() != null && reward.getStock() != -1) {
                reward.setStock(reward.getStock() - 1);
                rewardMapper.updateById(reward);
            }

            ExchangeRecord record = new ExchangeRecord();
            record.setChildId(childId);
            record.setRewardId(rewardId);
            record.setRewardName(reward.getName());
            record.setPoints(reward.getPoints());
            record.setStatus(0);
            record.setCreateTime(LocalDateTime.now());
            exchangeRecordMapper.insert(record);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Transactional
    public void deliver(Long id) {
        ExchangeRecord record = exchangeRecordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("兑换记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new RuntimeException("该记录已处理");
        }

        record.setStatus(1);
        record.setDeliverTime(LocalDateTime.now());
        exchangeRecordMapper.updateById(record);
    }

    @Transactional
    public void cancel(Long id) {
        ExchangeRecord record = exchangeRecordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("兑换记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new RuntimeException("该记录已处理，无法取消");
        }

        Child child = childMapper.selectById(record.getChildId());
        if (child != null) {
            child.setPoints(child.getPoints() + record.getPoints());
            childMapper.updateById(child);
        }

        Reward reward = rewardMapper.selectById(record.getRewardId());
        if (reward != null && reward.getStock() != null && reward.getStock() != -1) {
            reward.setStock(reward.getStock() + 1);
            rewardMapper.updateById(reward);
        }

        record.setStatus(2);
        exchangeRecordMapper.updateById(record);
    }
}
