package com.parentkid.checkin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("child_achievement")
public class ChildAchievement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long childId;

    private Long achievementId;

    private LocalDateTime unlockTime;
}
