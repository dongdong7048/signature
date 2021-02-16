package com.signature.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class History {
    private Integer serno;
    private String inputfile;
    private String action;
    private Timestamp begintime;
    private Timestamp endtime;
    private String usertype;
    private String outputfile;
    private String memo;
}
