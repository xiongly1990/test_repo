package com.parentkid.checkin.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class TaskDTO {

    private Long id;

    private Long childId;

    private String name;

    private String icon;

    private Integer cycleType;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer needPhoto;

    private Integer points;

    private Integer sort;

    private Integer status;
}
