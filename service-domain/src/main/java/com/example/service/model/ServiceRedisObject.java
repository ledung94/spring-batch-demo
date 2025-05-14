package com.example.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRedisObject {

    private String serviceCode;
    private String dataSource;
    private Integer ibService;
    private Integer cdpService;
    private Integer needTemp;
    private String tempTitle;
    private String tempContent;
    private String alertType;
    private String tempType;
    private String tempTitleEn;
    private String tempContentEn;
    private String sender;
    private Priority priority;
}
