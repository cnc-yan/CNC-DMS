package com.cnc.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 社員更新リクエスト DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {

    /** 社員ID（必須） */
    private Long id;

    /** 社員名 */
    private String empName;

    /** 性別 */
    private String gender;

    /** 国籍 */
    private String nationality;

    /** 所属部門ID */
    private Long deptId;

    /** 電話番号 */
    private String phone;

    /** メール */
    private String email;

    /** 社員状態 */
    private Integer empStatus;

    /** 入社日 */
    private LocalDate hireDate;

    /** 楽観ロックバージョン（必須） */
    private Long version;

    /** 更新者 */
    private String updateBy;
}
