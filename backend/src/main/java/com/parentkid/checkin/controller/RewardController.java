package com.parentkid.checkin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parentkid.checkin.common.Result;
import com.parentkid.checkin.dto.RewardDTO;
import com.parentkid.checkin.entity.Reward;
import com.parentkid.checkin.entity.RewardCategory;
import com.parentkid.checkin.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rewards")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @GetMapping("/categories")
    public Result<List<RewardCategory>> categories() {
        List<RewardCategory> list = rewardService.getCategories();
        return Result.success(list);
    }

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        Page<Reward> page = rewardService.getPage(pageNum, pageSize, categoryId, keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Reward> detail(@PathVariable Long id) {
        Reward reward = rewardService.getById(id);
        return Result.success(reward);
    }

    @PostMapping
    public Result<Void> add(@RequestBody RewardDTO dto) {
        if (dto.getCategoryId() == null) {
            return Result.error("请选择分类");
        }
        if (dto.getName() == null || dto.getName().isEmpty()) {
            return Result.error("请输入奖励名称");
        }
        if (dto.getPoints() == null || dto.getPoints() <= 0) {
            return Result.error("请输入有效的积分");
        }
        rewardService.add(dto);
        return Result.success("添加成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RewardDTO dto) {
        dto.setId(id);
        rewardService.update(dto);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        rewardService.delete(id);
        return Result.success("删除成功", null);
    }
}
