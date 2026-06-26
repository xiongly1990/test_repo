package com.parentkid.checkin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exchange_record")
public class ExchangeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long childId;

    private Long rewardId;

    private String rewardName;

    private Integer points;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime deliverTime;
}
