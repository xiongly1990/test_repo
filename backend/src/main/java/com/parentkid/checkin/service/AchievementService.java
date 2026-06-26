package com.parentkid.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parentkid.checkin.entity.Achievement;
import com.parentkid.checkin.entity.Child;
import com.parentkid.checkin.entity.ChildAchievement;
import com.parentkid.checkin.mapper.AchievementMapper;
import com.parentkid.checkin.mapper.ChildAchievementMapper;
import com.parentkid.checkin.mapper.ChildMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AchievementService {

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private ChildAchievementMapper childAchievementMapper;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public List<Achievement> getAllAchievements() {
        String cacheKey = "achievement:all";
        Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
        if (cachedObj != null) {
            try {
                String json = objectMapper.writeValueAsString(cachedObj);
                return objectMapper.readValue(json, new TypeReference<List<Achievement>>() {});
            } catch (Exception e) {
                // 如果转换失败，返回null让它重新从数据库获取
            }
        }
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Achievement::getLevel, Achievement::getSort);
        List<Achievement> list = achievementMapper.selectList(wrapper);
        redisTemplate.opsForValue().set(cacheKey, list, 1, TimeUnit.HOURS);
        return list;
    }

    public List<ChildAchievement> getChildAchievements(Long childId) {
        LambdaQueryWrapper<ChildAchievement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChildAchievement::getChildId, childId);
        return childAchievementMapper.selectList(wrapper);
    }

    public Map<String, Object> getAchievementTree(Long childId) {
        List<Achievement> allAchievements = getAllAchievements();
        if (allAchievements == null) {
            allAchievements = List.of();
        }
        List<ChildAchievement> childAchievements = getChildAchievements(childId);

        List<Long> unlockedIds = childAchievements.stream()
                .map(ChildAchievement::getAchievementId)
                .toList();

        List<Map<String, Object>> tree = new ArrayList<>();
        int currentLevel = 0;

        for (Achievement ach : allAchievements) {
            boolean unlocked = unlockedIds.contains(ach.getId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", ach.getId());
            item.put("name", ach.getName());
            item.put("description", ach.getDescription());
            item.put("icon", ach.getIcon());
            item.put("level", ach.getLevel());
            item.put("points", ach.getPoints());
            item.put("targetValue", ach.getTargetValue());
            item.put("unlocked", unlocked);
            tree.add(item);
            if (unlocked && ach.getLevel() > currentLevel) {
                currentLevel = ach.getLevel();
            }
        }

        Child child = childMapper.selectById(childId);
        int streakDays = child != null && child.getStreakDays() != null ? child.getStreakDays() : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("tree", tree);
        result.put("currentLevel", currentLevel);
        result.put("streakDays", streakDays);
        result.put("totalAchievements", allAchievements.size());
        result.put("unlockedCount", unlockedIds.size());
        return result;
    }

    @Transactional
    public void checkAndUnlock(Long childId) {
        Child child = childMapper.selectById(childId);
        if (child == null) return;

        int streakDays = child.getStreakDays() != null ? child.getStreakDays() : 0;

        List<Achievement> allAchievements = getAllAchievements();
        if (allAchievements == null) {
            return;
        }
        List<ChildAchievement> childAchievements = getChildAchievements(childId);
        List<Long> unlockedIds = childAchievements.stream()
                .map(ChildAchievement::getAchievementId)
                .toList();

        for (Achievement ach : allAchievements) {
            if (unlockedIds.contains(ach.getId())) continue;

            if (ach.getType() == 1 && streakDays >= ach.getTargetValue()) {
                ChildAchievement ca = new ChildAchievement();
                ca.setChildId(childId);
                ca.setAchievementId(ach.getId());
                ca.setUnlockTime(LocalDateTime.now());
                childAchievementMapper.insert(ca);

                if (ach.getPoints() != null && ach.getPoints() > 0) {
                    child.setPoints(child.getPoints() + ach.getPoints());
                    childMapper.updateById(child);
                }
            }
        }
    }
}