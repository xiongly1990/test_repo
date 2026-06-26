package com.parentkid.checkin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("child")
public class Child {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private LocalDate birthday;

    private String grade;

    private String avatar;

    private Integer points;

    private Integer streakDays;

    private Integer totalDays;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
