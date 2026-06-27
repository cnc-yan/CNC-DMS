package com.cnc.dms.service.impl;

import com.cnc.dms.dto.DormitoryCreateRequest;
import com.cnc.dms.dto.DormitoryQueryRequest;
import com.cnc.dms.dto.DormitoryUpdateRequest;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.Dormitory;
import com.cnc.dms.mapper.DormitoryMapper;
import com.cnc.dms.mapper.RoomMapper;
import com.cnc.dms.service.DormitoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 寮 Service 実装
 */
@Service
public class DormitoryServiceImpl implements DormitoryService {

    private final DormitoryMapper dormitoryMapper;
    private final RoomMapper roomMapper;

    public DormitoryServiceImpl(DormitoryMapper dormitoryMapper,
                                RoomMapper roomMapper) {
        this.dormitoryMapper = dormitoryMapper;
        this.roomMapper = roomMapper;
    }

    @Override
    public Dormitory findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("寮IDは必須です");
        }
        return dormitoryMapper.findById(id);
    }

    @Override
    public PageResponse<Dormitory> listDormitories(DormitoryQueryRequest query) {
        if (query == null) {
            query = DormitoryQueryRequest.builder().build();
        }
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }

        int offset = (query.getPageNum() - 1) * query.getPageSize();

        List<Dormitory> list = dormitoryMapper.findAll(
                query.getDormName(),
                query.getRegion(),
                query.getStatus(),
                offset,
                query.getPageSize()
        );

        Long total = dormitoryMapper.countByCondition(
                query.getDormName(),
                query.getRegion(),
                query.getStatus()
        );

        int totalPage = (int) ((total + query.getPageSize() - 1) / query.getPageSize());

        return PageResponse.<Dormitory>builder()
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .total(total)
                .totalPage(totalPage)
                .list(list)
                .build();
    }

    @Override
    public int createDormitory(DormitoryCreateRequest request) {
        if (request == null || request.getDormName() == null || request.getDormName().isBlank()) {
            throw new IllegalArgumentException("寮名称は必須です");
        }
        if (request.getRegion() == null || request.getRegion().isBlank()) {
            throw new IllegalArgumentException("地域は必須です");
        }

        Dormitory dormitory = Dormitory.builder()
                .dormName(request.getDormName())
                .region(request.getRegion())
                .address(request.getAddress())
                .dormCondition(request.getDormCondition())
                .totalRooms(request.getTotalRooms())
                .status(1)
                .createBy(request.getCreateBy())
                .build();

        return dormitoryMapper.insert(dormitory);
    }

    @Override
    public int updateDormitory(DormitoryUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("寮IDは必須です");
        }
        if (request.getVersion() == null) {
            throw new IllegalArgumentException("バージョン情報は必須です");
        }

        Dormitory existing = dormitoryMapper.findById(request.getId());
        if (existing == null) {
            throw new RuntimeException("寮が存在しません");
        }

        Dormitory dormitory = Dormitory.builder()
                .id(request.getId())
                .dormName(request.getDormName())
                .region(request.getRegion())
                .address(request.getAddress())
                .dormCondition(request.getDormCondition())
                .totalRooms(request.getTotalRooms())
                .status(request.getStatus())
                .version(request.getVersion())
                .updateBy(request.getUpdateBy())
                .build();

        int rows = dormitoryMapper.update(dormitory);
        if (rows == 0) {
            throw new RuntimeException("更新に失敗しました（楽観ロック競合）");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDormitory(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("寮IDは必須です");
        }

        Dormitory existing = dormitoryMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("寮が存在しません");
        }

        // 寮に属する部屋の存在チェック
        Long roomCount = roomMapper.countByCondition(existing.getId(), null, null);
        if (roomCount != null && roomCount > 0) {
            throw new RuntimeException(
                    "寮「" + existing.getDormName() + "」には " + roomCount
                    + " 件の部屋が存在するため削除できません。先に部屋を削除してください。"
            );
        }

        return dormitoryMapper.deleteById(id);
    }
}
