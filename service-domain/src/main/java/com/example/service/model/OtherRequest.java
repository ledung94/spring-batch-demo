package com.example.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OtherRequest {

    private String channel;

    private List<String> phones;
    private List<String> emails;
    private String title;
    private String content;
    private String msgCode;
    private String cifNumber;
    private List<Params> params;
    private String msgId;
    private String htmlContent;
    private String userId;
    private Object data;
    private String image;
    // flow retry sms Card
    private Boolean isRetrySms;
    private Long idZaloMessage;
    private String type;

    private String traceId;
    private Integer isCallback;

}

