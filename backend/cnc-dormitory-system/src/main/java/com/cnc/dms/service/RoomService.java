package com.cnc.dms.service;

import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.RoomCreateRequest;
import com.cnc.dms.dto.RoomQueryRequest;
import com.cnc.dms.dto.RoomUpdateRequest;
import com.cnc.dms.entity.Room;

import java.util.List;

/**
 * 部屋 Service 接口
 */
public interface RoomService {

    /**
     * IDで部屋を検索
     */
    Room findById(Long id);

    /**
     * 条件付きページング検索
     */
    PageResponse<Room> listRooms(RoomQueryRequest query);

    /**
     * 某寮に属する部屋一覧を取得
     */
    List<Room> findByDormitoryId(Long dormitoryId);

    /**
     * 新規作成
     */
    int createRoom(RoomCreateRequest request);

    /**
     * 更新
     */
    int updateRoom(RoomUpdateRequest request);

    /**
     * IDで削除
     */
    int deleteRoom(Long id);
}
