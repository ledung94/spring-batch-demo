package com.example.service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SERVICE")
@Data
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id = "SERVICE_ID_SEQ.NEXTVAL";

    @Column(name = "SERVICE_CODE", nullable = false, length = 50, unique = true)
    private String serviceCode;

    @Column(name = "SERVICE_NAME", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "SERVICE_TYPE", nullable = false, length = 20)
    private String serviceType;

    @Column(name = "SERVICE_DESC", length = 500)
    private String serviceDesc;

    @Column(name = "DATA_SOURCE", nullable = false, length = 50)
    private String dataSource;

    @Column(name = "SERVICE_FEE", precision = 10, scale = 2)
    private BigDecimal serviceFee;

    @Column(name = "SERVICE_API", length = 200)
    private String serviceApi;

    @Column(name = "CREATE_BY", nullable = false, length = 50)
    private String createBy = "ADMIN";

    @Column(name = "CREATE_TIME", nullable = false)
    private String createTime = "CURRENT_DATE";

    @Column(name = "UPDATE_BY", length = 50)
    private String updateBy = "ADMIN";

    @Column(name = "UPDATE_TIME")
    private String updateTime = "CURRENT_DATE";

    @Column(name = "STATUS", nullable = false, length = 1)
    private String status = "A";

    @Column(name = "APPROVE_BY", length = 50)
    private String approveBy;

    @Column(name = "APPROVE_TIME")
    private LocalDateTime approveTime;

    @Column(name = "REJECT_BY", length = 50)
    private String rejectBy;

    @Column(name = "REJECT_TIME")
    private LocalDateTime rejectTime;

    @Column(name = "REJECT_CONTENT", length = 500)
    private String rejectContent;

    @Column(name = "REMARK", length = 500)
    private String remark;

    @Column(name = "PRIORITY")
    private Double priority;

    @Column(name = "TOPIC", length = 20)
    private String topic;

    @Column(name = "CHANNEL_ID", length = 20)
    private String channelId;

    @Column(name = "NEED_TEMP")
    private Double needTemp = 1.0;

    @Column(name = "IB_SERVICE", length = 2)
    private Double ibService = 1.0;

    @Column(name = "CDP_SERVICE")
    private Double cdpService = (double) 0;

    // Getters and setters
}

