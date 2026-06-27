package com.cnc.dms.service;

import com.cnc.dms.dto.DormitoryCreateRequest;
import com.cnc.dms.dto.DormitoryQueryRequest;
import com.cnc.dms.dto.DormitoryUpdateRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.Dormitory;

/**
 * 寮 Service 接口
 */
public interface DormitoryService {

    /**
     * IDで寮を検索
     */
    Dormitory findById(Long id);

    /**
     * 条件付きページング検索
     */
    PageResponse<Dormitory> listDormitories(DormitoryQueryRequest query);

    /**
     * 新規作成
     */
    int createDormitory(DormitoryCreateRequest request);

    /**
     * 更新
     */
    int updateDormitory(DormitoryUpdateRequest request);

    /**
     * IDで削除
     */
    int deleteDormitory(Long id);
}
