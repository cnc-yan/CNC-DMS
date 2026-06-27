package com.cnc.dms.service.impl;

import com.cnc.dms.dto.*;
import com.cnc.dms.entity.Dormitory;
import com.cnc.dms.entity.Room;
import com.cnc.dms.mapper.DormitoryMapper;
import com.cnc.dms.mapper.RoomMapper;
import com.cnc.dms.mapper.ResidentRecordMapper;
import com.cnc.dms.mapper.EmployeeMapper;
import com.cnc.dms.service.ReportService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 帳票・統計 Service 実装
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final DormitoryMapper dormitoryMapper;
    private final RoomMapper roomMapper;
    private final ResidentRecordMapper residentRecordMapper;
    private final EmployeeMapper employeeMapper;

    public ReportServiceImpl(DormitoryMapper dormitoryMapper,
                             RoomMapper roomMapper,
                             ResidentRecordMapper residentRecordMapper,
                             EmployeeMapper employeeMapper) {
        this.dormitoryMapper = dormitoryMapper;
        this.roomMapper = roomMapper;
        this.residentRecordMapper = residentRecordMapper;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public SummaryReportResponse getSummary() {
        // 総寮数
        Long totalDormitories = dormitoryMapper.countTotal();

        // 総部屋数
        List<Room> allRooms = roomMapper.findAll(null, null, null, null, null);
        Long totalRooms = (long) allRooms.size();

        // 空室数（status=1 かつ currentOccupancy < capacity）
        Long vacantRooms = roomMapper.countVacantRooms(null, null);

        // 現在入居者数
        Long currentResidents = residentRecordMapper.countActive();

        // 全入居履歴件数
        Long totalResidentRecords = residentRecordMapper.countTotalRecords();

        // 未入居社員数
        Long employeesWithoutResident = residentRecordMapper.countEmployeesWithoutResident();

        return SummaryReportResponse.builder()
                .totalDormitories(totalDormitories)
                .totalRooms(totalRooms)
                .vacantRooms(vacantRooms)
                .currentResidents(currentResidents)
                .totalResidentRecords(totalResidentRecords)
                .employeesWithoutResident(employeesWithoutResident)
                .build();
    }

    @Override
    public List<OccupancyReportResponse> getOccupancyReport(ReportQueryRequest query) {
        // アクティブな全寮を取得
        List<Dormitory> dormitories = dormitoryMapper.findAll(null, null, 1, null, null);
        List<OccupancyReportResponse> reportList = new ArrayList<>();

        for (Dormitory dorm : dormitories) {
            // 条件でフィルタリング
            if (query != null && query.getRegion() != null && !query.getRegion().isBlank()
                    && !query.getRegion().equals(dorm.getRegion())) {
                continue;
            }

            // 寮に属する部屋一覧
            List<Room> rooms = roomMapper.findByDormitoryId(dorm.getId());

            int totalRooms = rooms.size();
            int occupiedRooms = (int) rooms.stream()
                    .filter(r -> r.getStatus() != null && r.getStatus() == 2)
                    .count();
            int vacantRoomsCount = (int) rooms.stream()
                    .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                    .count();

            // 定員ベースの稼働率: 全室の定員合計に対する現在入居者数の割合
            int totalCapacity = rooms.stream()
                    .mapToInt(r -> r.getCapacity() != null ? r.getCapacity() : 0)
                    .sum();
            int totalOccupancy = rooms.stream()
                    .mapToInt(r -> r.getCurrentOccupancy() != null ? r.getCurrentOccupancy() : 0)
                    .sum();

            double occupancyRate = totalCapacity > 0
                    ? Math.round((double) totalOccupancy / totalCapacity * 100.0 * 10.0) / 10.0
                    : 0.0;

            reportList.add(OccupancyReportResponse.builder()
                    .dormitoryId(dorm.getId())
                    .dormName(dorm.getDormName())
                    .region(dorm.getRegion())
                    .totalRooms(totalRooms)
                    .occupiedRooms(occupiedRooms)
                    .vacantRooms(vacantRoomsCount)
                    .totalCapacity(totalCapacity)
                    .totalOccupancy(totalOccupancy)
                    .occupancyRate(occupancyRate)
                    .build());
        }

        return reportList;
    }

}
