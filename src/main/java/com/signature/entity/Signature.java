package com.signature.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;



@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Signature {
          private Integer id;
          private String usertype;
          private String filename;
          private String filepath;
          private Timestamp createtime;
          private Timestamp updatetime;
          private String savetype;
          private String memo;
          private Integer totalpages;
          private String issigned;


}
