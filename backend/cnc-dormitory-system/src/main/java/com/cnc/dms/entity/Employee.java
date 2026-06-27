package com.cnc.dms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 社員 Entity
 * 対応テーブル: tbl_employee
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    private Long id;
    private String empNo;
    private String empName;
    private String gender;
    private String nationality;
    private Long deptId;
    private String deptName;
    private LocalDate firstUseDate;
    private String phone;
    private String email;
    private LocalDate hireDate;
    private LocalDate resignDate;
    private Integer empStatus;
    private Long version;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
