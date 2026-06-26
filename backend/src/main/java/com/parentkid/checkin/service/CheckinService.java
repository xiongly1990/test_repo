package com.parentkid.checkin.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parentkid.checkin.entity.CheckinRecord;
import com.parentkid.checkin.entity.Child;
import com.parentkid.checkin.entity.Task;
import com.parentkid.checkin.mapper.CheckinRecordMapper;
import com.parentkid.checkin.mapper.ChildMapper;
import com.parentkid.checkin.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CheckinService {

    @Autowired
    private CheckinRecordMapper checkinRecordMapper;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AchievementService achievementService;

    public Page<CheckinRecord> getPendingList(int pageNum, int pageSize) {
        Page<CheckinRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getStatus, 0)
                .orderByDesc(CheckinRecord::getCreateTime);
        return checkinRecordMapper.selectPage(page, wrapper);
    }

    public Page<CheckinRecord> getRecords(int pageNum, int pageSize, Long childId, Integer status) {
        Page<CheckinRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        if (childId != null) {
            wrapper.eq(CheckinRecord::getChildId, childId);
        }
        if (status != null) {
            wrapper.eq(CheckinRecord::getStatus, status);
        }
        wrapper.orderByDesc(CheckinRecord::getCheckinTime);
        return checkinRecordMapper.selectPage(page, wrapper);
    }

    public Page<CheckinRecord> getSupplementRecords(int pageNum, int pageSize, Long childId) {
        Page<CheckinRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getIsSupplement, 1);
        if (childId != null) {
            wrapper.eq(CheckinRecord::getChildId, childId);
        }
        wrapper.orderByDesc(CheckinRecord::getCheckinTime);
        return checkinRecordMapper.selectPage(page, wrapper);
    }

    @Transactional
    public void submitCheckin(Long childId, Long taskId, String photoUrl) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!task.getChildId().equals(childId)) {
            throw new RuntimeException("任务不属于该孩子");
        }

        String dateStr = DateUtil.today();
        String lockKey = "checkin:lock:" + childId + ":" + taskId + ":" + dateStr;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            throw new RuntimeException("请勿重复打卡");
        }

        try {
            LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CheckinRecord::getChildId, childId)
                    .eq(CheckinRecord::getTaskId, taskId)
                    .eq(CheckinRecord::getCheckinDate, LocalDate.now());
            Long count = checkinRecordMapper.selectCount(wrapper);
            if (count > 0) {
                throw new RuntimeException("今日已打卡");
            }

            CheckinRecord record = new CheckinRecord();
            record.setChildId(childId);
            record.setTaskId(taskId);
            record.setTaskName(task.getName());
            record.setPoints(task.getPoints());
            record.setPhotoUrl(photoUrl);
            record.setStatus(task.getNeedPhoto() == 1 ? 0 : 1);
            record.setCheckinDate(LocalDate.now());
            record.setCheckinTime(LocalDateTime.now());
            record.setIsSupplement(0);
            checkinRecordMapper.insert(record);

            if (task.getNeedPhoto() == 0) {
                addPoints(childId, task.getPoints());
                updateStreakOnCheckin(childId);
                achievementService.checkAndUnlock(childId);
            }
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Transactional
    public void approve(Long id, Long auditorId, String remark) {
        CheckinRecord record = checkinRecordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("打卡记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new RuntimeException("该记录已审核");
        }

        record.setStatus(1);
        record.setAuditorId(auditorId);
        record.setAuditTime(LocalDateTime.now());
        record.setAuditRemark(remark);
        checkinRecordMapper.updateById(record);

        addPoints(record.getChildId(), record.getPoints());
        updateStreakOnCheckin(record.getChildId());
        achievementService.checkAndUnlock(record.getChildId());
    }

    @Transactional
    public void reject(Long id, Long auditorId, String remark) {
        CheckinRecord record = checkinRecordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("打卡记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new RuntimeException("该记录已审核");
        }

        record.setStatus(2);
        record.setAuditorId(auditorId);
        record.setAuditTime(LocalDateTime.now());
        record.setAuditRemark(remark);
        checkinRecordMapper.updateById(record);
    }

    @Transactional
    public void supplement(Long childId, Long taskId, String reason, String checkinDate) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        LocalDate date = checkinDate != null ? LocalDate.parse(checkinDate) : LocalDate.now();

        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getChildId, childId)
                .eq(CheckinRecord::getTaskId, taskId)
                .eq(CheckinRecord::getCheckinDate, date);
        Long count = checkinRecordMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("该日期已有打卡记录");
        }

        CheckinRecord record = new CheckinRecord();
        record.setChildId(childId);
        record.setTaskId(taskId);
        record.setTaskName(task.getName());
        record.setPoints(task.getPoints());
        record.setStatus(1);
        record.setCheckinDate(date);
        record.setCheckinTime(LocalDateTime.now());
        record.setIsSupplement(1);
        record.setSupplementReason(reason);
        record.setAuditTime(LocalDateTime.now());
        checkinRecordMapper.insert(record);

        addPoints(childId, task.getPoints());
        updateStreakOnCheckin(childId);
        achievementService.checkAndUnlock(childId);
    }

    private void addPoints(Long childId, int points) {
        Child child = childMapper.selectById(childId);
        if (child != null) {
            child.setPoints(child.getPoints() + points);
            child.setTotalDays(child.getTotalDays() + 1);
            childMapper.updateById(child);
            String streakKey = "child:streak:" + childId;
            redisTemplate.delete(streakKey);
        }
    }

    private void updateStreakOnCheckin(Long childId) {
        String streakKey = "child:streak:" + childId;
        redisTemplate.delete(streakKey);

        Child child = childMapper.selectById(childId);
        if (child == null) return;

        LocalDate today = LocalDate.now();
        int streakDays = 0;

        for (int i = 0; i < 365; i++) {
            LocalDate checkDate = today.minusDays(i);
            LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CheckinRecord::getChildId, childId)
                    .eq(CheckinRecord::getCheckinDate, checkDate)
                    .eq(CheckinRecord::getStatus, 1);
            Long count = checkinRecordMapper.selectCount(wrapper);
            if (count > 0) {
                streakDays++;
            } else {
                break;
            }
        }

        child.setStreakDays(streakDays);
        childMapper.updateById(child);

        redisTemplate.opsForValue().set(streakKey, streakDays, 1, TimeUnit.HOURS);
    }

    public int getStreakDays(Long childId) {
        String streakKey = "child:streak:" + childId;
        Object cached = redisTemplate.opsForValue().get(streakKey);
        if (cached != null) {
            return Integer.parseInt(cached.toString());
        }
        Child child = childMapper.selectById(childId);
        int streak = child != null && child.getStreakDays() != null ? child.getStreakDays() : 0;
        redisTemplate.opsForValue().set(streakKey, streak, 1, TimeUnit.HOURS);
        return streak;
    }

    public List<CheckinRecord> getTodayRecordStatus(Long childId) {
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getChildId, childId)
                .eq(CheckinRecord::getCheckinDate, LocalDate.now());
        return checkinRecordMapper.selectList(wrapper);
    }
}
