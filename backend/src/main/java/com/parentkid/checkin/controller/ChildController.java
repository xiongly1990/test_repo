package com.parentkid.checkin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parentkid.checkin.common.Result;
import com.parentkid.checkin.dto.ChildDTO;
import com.parentkid.checkin.entity.Child;
import com.parentkid.checkin.service.ChildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/children")
public class ChildController {

    @Autowired
    private ChildService childService;

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Child> page = childService.getPage(pageNum, pageSize, keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Child> detail(@PathVariable Long id) {
        Child child = childService.getById(id);
        return Result.success(child);
    }

    @PostMapping
    public Result<Void> add(@RequestBody ChildDTO dto) {
        if (dto.getName() == null || dto.getName().isEmpty()) {
            return Result.error("请输入孩子姓名");
        }
        childService.add(dto);
        return Result.success("添加成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ChildDTO dto) {
        dto.setId(id);
        childService.update(dto);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        childService.delete(id);
        return Result.success("删除成功", null);
    }
}
