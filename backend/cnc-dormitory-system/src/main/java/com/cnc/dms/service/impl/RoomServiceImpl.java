package com.cnc.dms.service.impl;

import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.RoomCreateRequest;
import com.cnc.dms.dto.RoomQueryRequest;
import com.cnc.dms.dto.RoomUpdateRequest;
import com.cnc.dms.entity.Room;
import com.cnc.dms.mapper.ResidentRecordMapper;
import com.cnc.dms.mapper.RoomMapper;
import com.cnc.dms.service.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 部屋 Service 実装
 */
@Service
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;
    private final ResidentRecordMapper residentRecordMapper;

    public RoomServiceImpl(RoomMapper roomMapper,
                           ResidentRecordMapper residentRecordMapper) {
        this.roomMapper = roomMapper;
        this.residentRecordMapper = residentRecordMapper;
    }

    @Override
    public Room findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("部屋IDは必須です");
        }
        return roomMapper.findById(id);
    }

    @Override
    public PageResponse<Room> listRooms(RoomQueryRequest query) {
        if (query == null) {
            query = RoomQueryRequest.builder().build();
        }
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }

        int offset = (query.getPageNum() - 1) * query.getPageSize();

        List<Room> list = roomMapper.findAll(
                query.getDormitoryId(),
                query.getRoomNumber(),
                query.getStatus(),
                offset,
                query.getPageSize()
        );

        Long total = roomMapper.countByCondition(
                query.getDormitoryId(),
                query.getRoomNumber(),
                query.getStatus()
        );

        int totalPage = (int) ((total + query.getPageSize() - 1) / query.getPageSize());

        return PageResponse.<Room>builder()
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .total(total)
                .totalPage(totalPage)
                .list(list)
                .build();
    }

    @Override
    public List<Room> findByDormitoryId(Long dormitoryId) {
        if (dormitoryId == null) {
            throw new IllegalArgumentException("寮IDは必須です");
        }
        return roomMapper.findByDormitoryId(dormitoryId);
    }

    @Override
    public int createRoom(RoomCreateRequest request) {
        if (request == null || request.getDormitoryId() == null) {
            throw new IllegalArgumentException("所属寮IDは必須です");
        }
        if (request.getRoomNumber() == null || request.getRoomNumber().isBlank()) {
            throw new IllegalArgumentException("部屋番号は必須です");
        }

        Room room = Room.builder()
                .dormitoryId(request.getDormitoryId())
                .roomNumber(request.getRoomNumber())
                .capacity(request.getCapacity() != null ? request.getCapacity() : 1)
                .dailyRate(request.getDailyRate())
                .roomType(request.getRoomType())
                .acType(request.getAcType())
                .memo1(request.getMemo1())
                .memo2(request.getMemo2())
                .memo3(request.getMemo3())
                .status(1)
                .createBy(request.getCreateBy())
                .build();

        return roomMapper.insert(room);
    }

    @Override
    public int updateRoom(RoomUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("部屋IDは必須です");
        }
        if (request.getVersion() == null) {
            throw new IllegalArgumentException("バージョン情報は必須です");
        }

        Room existing = roomMapper.findById(request.getId());
        if (existing == null) {
            throw new RuntimeException("部屋が存在しません");
        }

        Room room = Room.builder()
                .id(request.getId())
                .roomNumber(request.getRoomNumber())
                .capacity(request.getCapacity())
                .dailyRate(request.getDailyRate())
                .roomType(request.getRoomType())
                .acType(request.getAcType())
                .memo1(request.getMemo1())
                .memo2(request.getMemo2())
                .memo3(request.getMemo3())
                .status(request.getStatus())
                .version(request.getVersion())
                .updateBy(request.getUpdateBy())
                .build();

        int rows = roomMapper.update(room);
        if (rows == 0) {
            throw new RuntimeException("更新に失敗しました（楽観ロック競合）");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoom(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("部屋IDは必須です");
        }

        Room existing = roomMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("部屋が存在しません");
        }

        // 入居履歴の存在チェック
        Long residentCount = residentRecordMapper.countByCondition(null, id, null);
        if (residentCount != null && residentCount > 0) {
            throw new RuntimeException(
                    "部屋「" + existing.getRoomNumber() + "号室」には " + residentCount
                    + " 件の入居履歴が存在するため削除できません。先に入居履歴を削除してください。"
            );
        }

        return roomMapper.deleteById(id);
    }
}
