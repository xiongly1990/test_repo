package com.parentkid.checkin.dto;

import lombok.Data;

@Data
public class RewardDTO {

    private Long id;

    private Long categoryId;

    private String name;

    private String icon;

    private Integer points;

    private Integer stock;

    private String description;

    private Integer sort;

    private Integer status;
}
