package com.example.report.batch.entity;

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
@Entity(name="MSG_LOG")
public class MsgLog {
    @Id
    @Column(name="LOG_ID")
    private Long logId;

    @Column(name = "CONTENT")
    private String content;
}
