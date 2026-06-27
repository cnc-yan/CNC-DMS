package com.cnc.dms.mapper;

import com.cnc.dms.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 所属 Mapper 接口
 */
@Mapper
public interface DepartmentMapper {

    /**
     * IDで所属を検索
     */
    Department findById(@Param("id") Long id);

    /**
     * 条件付き一覧検索（ページング対応）
     */
    List<Department> findAll(@Param("deptCode") String deptCode,
                             @Param("deptName") String deptName,
                             @Param("region") String region,
                             @Param("status") Integer status,
                             @Param("offset") Integer offset,
                             @Param("limit") Integer limit);

    /**
     * 条件付き総件数取得
     */
    Long countByCondition(@Param("deptCode") String deptCode,
                          @Param("deptName") String deptName,
                          @Param("region") String region,
                          @Param("status") Integer status);

    /**
     * 所属コードで検索
     */
    Department findByDeptCode(@Param("deptCode") String deptCode);

    /**
     * 新規作成
     */
    int insert(Department department);

    /**
     * 更新
     */
    int update(Department department);

    /**
     * IDで削除
     */
    int deleteById(@Param("id") Long id);
}
