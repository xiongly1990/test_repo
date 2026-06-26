package com.parentkid.checkin.controller;

import com.parentkid.checkin.common.Result;
import com.parentkid.checkin.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/achievements")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @GetMapping("/tree")
    public Result<Map<String, Object>> tree(@RequestParam Long childId) {
        Map<String, Object> tree = achievementService.getAchievementTree(childId);
        return Result.success(tree);
    }

    @PostMapping("/check-unlock")
    public Result<Void> checkUnlock(@RequestParam Long childId) {
        achievementService.checkAndUnlock(childId);
        return Result.success(null);
    }
}
