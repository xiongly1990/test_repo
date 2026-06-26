package com.parentkid.checkin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parentkid.checkin.common.Result;
import com.parentkid.checkin.entity.ExchangeRecord;
import com.parentkid.checkin.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/exchange")
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;

    @GetMapping("/records")
    public Result<Map<String, Object>> records(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long childId,
            @RequestParam(required = false) Integer status) {
        Page<ExchangeRecord> page = exchangeService.getRecords(pageNum, pageSize, childId, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return Result.success(result);
    }

    @PostMapping
    public Result<Void> exchange(@RequestBody Map<String, Long> params) {
        Long childId = params.get("childId");
        Long rewardId = params.get("rewardId");
        if (childId == null) {
            return Result.error("请选择孩子");
        }
        if (rewardId == null) {
            return Result.error("请选择奖励");
        }
        try {
            exchangeService.exchange(childId, rewardId);
            return Result.success("兑换成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/deliver")
    public Result<Void> deliver(@PathVariable Long id) {
        try {
            exchangeService.deliver(id);
            return Result.success("已标记为已发放", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        try {
            exchangeService.cancel(id);
            return Result.success("已取消，积分已退回", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
