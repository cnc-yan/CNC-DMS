package com.cnc.dms.mapper;

import com.cnc.dms.entity.ImportLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * インポートログ Mapper
 */
@Mapper
public interface ImportLogMapper {

    ImportLog findById(@Param("id") Long id);

    List<ImportLog> findAll(@Param("importType") String importType,
                            @Param("importStatus") Integer importStatus,
                            @Param("offset") Integer offset,
                            @Param("limit") Integer limit);

    Long countByCondition(@Param("importType") String importType,
                          @Param("importStatus") Integer importStatus);

    int insert(ImportLog importLog);

    int update(ImportLog importLog);

    int deleteById(@Param("id") Long id);
}
