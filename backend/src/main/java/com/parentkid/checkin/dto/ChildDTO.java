package com.parentkid.checkin.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ChildDTO {

    private Long id;

    private String name;

    private LocalDate birthday;

    private String grade;

    private String avatar;
}
