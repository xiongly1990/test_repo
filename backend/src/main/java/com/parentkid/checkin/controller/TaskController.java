package com.parentkid.checkin.controller;

import com.parentkid.checkin.common.Result;
import com.parentkid.checkin.dto.TaskDTO;
import com.parentkid.checkin.entity.Task;
import com.parentkid.checkin.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public Result<List<Task>> list(@RequestParam Long childId) {
        List<Task> list = taskService.getByChildId(childId);
        return Result.success(list);
    }

    @GetMapping("/today")
    public Result<List<Task>> todayTasks(@RequestParam Long childId) {
        List<Task> list = taskService.getTodayTasks(childId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Task> detail(@PathVariable Long id) {
        Task task = taskService.getById(id);
        return Result.success(task);
    }

    @PostMapping
    public Result<Void> add(@RequestBody TaskDTO dto) {
        if (dto.getChildId() == null) {
            return Result.error("请选择孩子");
        }
        if (dto.getName() == null || dto.getName().isEmpty()) {
            return Result.error("请输入任务名称");
        }
        taskService.add(dto);
        return Result.success("添加成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TaskDTO dto) {
        dto.setId(id);
        taskService.update(dto);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success("删除成功", null);
    }
}
