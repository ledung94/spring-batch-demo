package com.example.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "API_MAPPER")
@Getter
@Setter
public class ApiMapper {
    @Id
    @Column(name = "ID")
    private Long id;
    @Column(name = "PARTNER_ID")
    private Long partnerId;
    @Column(name = "FROM_PROPERTY")
    private String fromProperty;
    @Column(name = "TO_PROPERTY")
    private String toProperty;
    @Column(name = "DATA_TYPE")
    private String dataType;
    @Column(name = "TO_PATTERN")
    private String toPattern;
    @Column(name = "FROM_PATTERN")
    private String fromPattern;
    @Column(name = "DEFAULT_VALUE")
    private String defaultValue;
    @Column(name = "ORDER")
    private Integer order;
    @Column(name = "IS_BODY")
    private Integer isBody;
    @Column(name = "IS_HEADER")
    private Integer isHeader;
    @Column(name = "PARENT_NAME")
    private String parentName;
    @Column(name = "IS_SIGNATURE")
    private Integer isSignature;
    @Column(name = "SIGNATURE_TYPE")
    private String signatureType;
}
