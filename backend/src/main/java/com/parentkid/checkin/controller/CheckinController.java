package com.parentkid.checkin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parentkid.checkin.common.Result;
import com.parentkid.checkin.entity.CheckinRecord;
import com.parentkid.checkin.service.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/checkin")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long childId = Long.valueOf(params.get("childId").toString());
        Long taskId = Long.valueOf(params.get("taskId").toString());
        String photoUrl = params.get("photoUrl") != null ? params.get("photoUrl").toString() : null;
        try {
            checkinService.submitCheckin(childId, taskId, photoUrl);
            return Result.success("打卡成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/pending")
    public Result<Map<String, Object>> pending(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<CheckinRecord> page = checkinService.getPendingList(pageNum, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return Result.success(result);
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id,
                                @RequestBody(required = false) Map<String, String> params,
                                HttpServletRequest request) {
        Long auditorId = (Long) request.getAttribute("userId");
        String remark = params != null && params.get("remark") != null ? params.get("remark") : null;
        try {
            checkinService.approve(id, auditorId, remark);
            return Result.success("审核通过", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestBody(required = false) Map<String, String> params,
                               HttpServletRequest request) {
        Long auditorId = (Long) request.getAttribute("userId");
        String remark = params != null && params.get("remark") != null ? params.get("remark") : null;
        try {
            checkinService.reject(id, auditorId, remark);
            return Result.success("已拒绝", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/records")
    public Result<Map<String, Object>> records(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long childId,
            @RequestParam(required = false) Integer status) {
        Page<CheckinRecord> page = checkinService.getRecords(pageNum, pageSize, childId, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return Result.success(result);
    }

    @PostMapping("/supplement")
    public Result<Void> supplement(@RequestBody Map<String, Object> params) {
        Long childId = Long.valueOf(params.get("childId").toString());
        Long taskId = Long.valueOf(params.get("taskId").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : "";
        String checkinDate = params.get("checkinDate") != null ? params.get("checkinDate").toString() : null;
        try {
            checkinService.supplement(childId, taskId, reason, checkinDate);
            return Result.success("补卡成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/supplement-records")
    public Result<Map<String, Object>> supplementRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long childId) {
        Page<CheckinRecord> page = checkinService.getSupplementRecords(pageNum, pageSize, childId);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return Result.success(result);
    }

    @GetMapping("/streak/{childId}")
    public Result<Integer> streak(@PathVariable Long childId) {
        int streak = checkinService.getStreakDays(childId);
        return Result.success(streak);
    }

    @GetMapping("/today-status")
    public Result<List<CheckinRecord>> todayStatus(@RequestParam Long childId) {
        List<CheckinRecord> list = checkinService.getTodayRecordStatus(childId);
        return Result.success(list);
    }
}
