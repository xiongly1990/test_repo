package com.parentkid.checkin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("checkin_record")
public class CheckinRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long childId;

    private Long taskId;

    private String taskName;

    private Integer points;

    private String photoUrl;

    private Integer status;

    private LocalDate checkinDate;

    private LocalDateTime checkinTime;

    private Integer isSupplement;

    private String supplementReason;

    private Long auditorId;

    private LocalDateTime auditTime;

    private String auditRemark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
