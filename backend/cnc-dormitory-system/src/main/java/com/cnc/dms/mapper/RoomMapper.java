package com.cnc.dms.mapper;

import com.cnc.dms.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部屋 Mapper 接口
 */
@Mapper
public interface RoomMapper {

    /**
     * IDで部屋を検索
     */
    Room findById(@Param("id") Long id);

    /**
     * 条件付き一覧検索（ページング対応）
     */
    List<Room> findAll(@Param("dormitoryId") Long dormitoryId,
                       @Param("roomNumber") String roomNumber,
                       @Param("status") Integer status,
                       @Param("offset") Integer offset,
                       @Param("limit") Integer limit);

    /**
     * 条件付き総件数取得
     */
    Long countByCondition(@Param("dormitoryId") Long dormitoryId,
                          @Param("roomNumber") String roomNumber,
                          @Param("status") Integer status);

    /**
     * 某寮に属する部屋一覧を取得
     */
    List<Room> findByDormitoryId(@Param("dormitoryId") Long dormitoryId);

    /**
     * 空き室一覧を検索（性別条件対応）
     */
    List<Room> findVacantRooms(@Param("dormitoryId") Long dormitoryId,
                               @Param("gender") String gender,
                               @Param("offset") Integer offset,
                               @Param("limit") Integer limit);

    /**
     * 空き室総件数
     */
    Long countVacantRooms(@Param("dormitoryId") Long dormitoryId,
                          @Param("gender") String gender);

    /**
     * 新規作成
     */
    int insert(Room room);

    /**
     * 更新
     */
    int update(Room room);

    /**
     * IDで削除
     */
    int deleteById(@Param("id") Long id);
}
