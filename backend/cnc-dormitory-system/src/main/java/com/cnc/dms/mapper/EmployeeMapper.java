package com.cnc.dms.mapper;

import com.cnc.dms.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社員 Mapper 接口
 */
@Mapper
public interface EmployeeMapper {

    /**
     * IDで社員を検索
     */
    Employee findById(@Param("id") Long id);

    /**
     * 工番で社員を検索
     */
    Employee findByEmpNo(@Param("empNo") String empNo);

    /**
     * 条件付き一覧検索（ページング対応）
     */
    List<Employee> findAll(@Param("empNo") String empNo,
                           @Param("empName") String empName,
                           @Param("deptId") Long deptId,
                           @Param("empStatus") Integer empStatus,
                           @Param("offset") Integer offset,
                           @Param("limit") Integer limit);

    /**
     * 条件付き総件数取得
     */
    Long countByCondition(@Param("empNo") String empNo,
                          @Param("empName") String empName,
                          @Param("deptId") Long deptId,
                          @Param("empStatus") Integer empStatus);

    /**
     * 新規作成
     */
    int insert(Employee employee);

    /**
     * 更新
     */
    int update(Employee employee);

    /**
     * IDで削除
     */
    int deleteById(@Param("id") Long id);
}
