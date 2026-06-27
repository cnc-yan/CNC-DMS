package com.cnc.dms.service.impl;

import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.VacancyQueryRequest;
import com.cnc.dms.dto.VacancyResponse;
import com.cnc.dms.entity.Room;
import com.cnc.dms.mapper.RoomMapper;
import com.cnc.dms.service.VacancyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 空き室管理 Service 実装
 */
@Service
public class VacancyServiceImpl implements VacancyService {

    private final RoomMapper roomMapper;

    public VacancyServiceImpl(RoomMapper roomMapper) {
        this.roomMapper = roomMapper;
    }

    @Override
    public PageResponse<VacancyResponse> listVacantRooms(VacancyQueryRequest query) {
        if (query == null) query = VacancyQueryRequest.builder().build();
        if (query.getPageNum() == null || query.getPageNum() < 1) query.setPageNum(1);
        if (query.getPageSize() == null || query.getPageSize() < 1) query.setPageSize(10);

        int offset = (query.getPageNum() - 1) * query.getPageSize();

        List<Room> rooms = roomMapper.findVacantRooms(
                query.getDormitoryId(),
                query.getGender(),
                offset,
                query.getPageSize()
        );

        Long total = roomMapper.countVacantRooms(
                query.getDormitoryId(),
                query.getGender()
        );

        int totalPage = (int) ((total + query.getPageSize() - 1) / query.getPageSize());

        List<VacancyResponse> list = rooms.stream().map(this::toVacancyResponse).collect(Collectors.toList());

        return PageResponse.<VacancyResponse>builder()
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .total(total)
                .totalPage(totalPage)
                .list(list)
                .build();
    }

    @Override
    public List<VacancyResponse> getAllVacantRooms(VacancyQueryRequest query) {
        if (query == null) query = VacancyQueryRequest.builder().build();

        List<Room> rooms = roomMapper.findVacantRooms(
                query.getDormitoryId(),
                query.getGender(),
                null,
                null
        );

        return rooms.stream().map(this::toVacancyResponse).collect(Collectors.toList());
    }

    @Override
    public boolean isRoomAvailable(Long roomId) {
        if (roomId == null) return false;
        Room room = roomMapper.findById(roomId);
        if (room == null) return false;
        return room.getStatus() == 1 && room.getCurrentOccupancy() < room.getCapacity();
    }

    private VacancyResponse toVacancyResponse(Room room) {
        return VacancyResponse.builder()
                .id(room.getId())
                .dormitoryId(room.getDormitoryId())
                .dormName(room.getDormName())
                .dormCondition(room.getDormCondition())
                .roomNumber(room.getRoomNumber())
                .capacity(room.getCapacity())
                .currentOccupancy(room.getCurrentOccupancy())
                .availableCapacity(room.getCapacity() - room.getCurrentOccupancy())
                .dailyRate(room.getDailyRate())
                .region(room.getRegion())
                .build();
    }
}
