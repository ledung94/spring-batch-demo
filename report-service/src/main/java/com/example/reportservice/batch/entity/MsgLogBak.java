package com.example.reportservice.batch.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="MSG_LOG_BAK")
public class MsgLogBak {
    @Id
    @Column(name="LOG_ID")
    private Long logId;

    @Column(name = "CONTENT")
    private String content;
}
