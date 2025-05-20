package com.example.common.model;

import lombok.Data;

@Data
public class Mapping {
    private Long id;
    private String apiCode;
    private String fromProperty;
    private String toProperty;
    private Integer isHeader;
    private Integer isBody;
    private Integer isQueryParam;
    private String dataType;
    private String defaultValue;
    private String format;
    private Integer order;
    private String status;
}
