package com.example.service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TEMPLATE")
@Data
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id = "TEMPLATE_ID_SEQ.NEXTVAL";

    @Column(name = "TEMP_CODE", nullable = false, length = 60)
    private String tempCode;

    @Column(name = "TEMP_NAME", nullable = false, length = 110)
    private String tempName;

    @Column(name = "TEMP_TYPE", length = 10)
    private String tempType;

    @Column(name = "TEMP_DESC", length = 100)
    private String tempDesc;

    @Column(name = "TEMP_TITLE", length = 200)
    private String tempTitle;

    @Lob
    @Column(name = "TEMP_CONTENT")
    private String tempContent;

    @Column(name = "TEMP_FIELDS", length = 50)
    private String tempFields;

    @Column(name = "TEMP_PATH", length = 50)
    private String tempPath;

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

    @Column(name = "SERVICE_ID")
    private String serviceId;

    @Column(name = "ALERT_TYPE", length = 10)
    private String alertType;

    @Column(name = "CAMPAIGN_ID")
    private Long campaignId;

    @Lob
    @Column(name = "FILE_ATTACH")
    private String fileAttach;

    @Column(name = "TEMP_TITLE_EN", length = 100)
    private String tempTitleEn;

    @Lob
    @Column(name = "TEMP_CONTENT_EN")
    private String tempContentEn;

    @Column(name = "SENDER", length = 100)
    private String sender;

    @Column(name = "PRICE", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "PRIORITY", length = 20)
    private String priority = "MEDIUM";

    // Getters and setters
}

