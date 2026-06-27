package com.cnc.dms.service.impl;

import com.cnc.dms.dto.ImportLogQueryRequest;
import com.cnc.dms.dto.ImportResultResponse;
import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.entity.*;
import com.cnc.dms.mapper.*;
import com.cnc.dms.service.ImportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Excelインポート Service 実装
 */
@Service
public class ImportServiceImpl implements ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportServiceImpl.class);

    private final ImportLogMapper importLogMapper;
    private final DormitoryMapper dormitoryMapper;
    private final RoomMapper roomMapper;
    private final EmployeeMapper employeeMapper;
    private final ResidentRecordMapper residentRecordMapper;

    public ImportServiceImpl(ImportLogMapper importLogMapper,
                             DormitoryMapper dormitoryMapper,
                             RoomMapper roomMapper,
                             EmployeeMapper employeeMapper,
                             ResidentRecordMapper residentRecordMapper) {
        this.importLogMapper = importLogMapper;
        this.dormitoryMapper = dormitoryMapper;
        this.roomMapper = roomMapper;
        this.employeeMapper = employeeMapper;
        this.residentRecordMapper = residentRecordMapper;
    }

    @Override
    public ImportResultResponse importExcel(MultipartFile file, String importType, String operatorId) {
        String fileName = file.getOriginalFilename();
        log.info("Excelインポート開始: fileName={}, importType={}, operatorId={}", fileName, importType, operatorId);

        // インポートログを作成（トランザクション外）
        ImportLog importLog = ImportLog.builder()
                .fileName(fileName)
                .importType(importType)
                .totalCount(0)
                .successCount(0)
                .errorCount(0)
                .importStatus(0) // 処理中
                .operatorId(operatorId)
                .createBy(operatorId)
                .version(0L)
                .build();
        importLogMapper.insert(importLog);

        List<String> errorMessages = new ArrayList<>();
        int totalCount = 0;
        int successCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new RuntimeException("Excelシートが見つかりません");
            }

            int rowCount = sheet.getPhysicalNumberOfRows();
            totalCount = Math.max(0, rowCount - 1);

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("ヘッダー行が見つかりません");
            }

            // 実際のデータインポート処理（トランザクション内）
            ImportResult result = doImport(sheet, importType, operatorId, errorMessages);
            successCount = result.successCount;
            totalCount = result.totalCount;

        } catch (Exception e) {
            log.error("インポート処理エラー: {}", e.getMessage());
            errorMessages.add(e.getMessage());
        }

        // インポートログを更新（トランザクション外）
        return updateImportLog(importLog, fileName, importType, totalCount, successCount, errorMessages, operatorId);
    }

    /**
     * 実際のデータインポート処理（トランザクション内）
     */
    @Transactional(rollbackFor = Exception.class)
    public ImportResult doImport(Sheet sheet, String importType, String operatorId, List<String> errorMessages) {
        int totalCount = Math.max(0, sheet.getPhysicalNumberOfRows() - 1);
        int successCount;

        switch (importType.toUpperCase()) {
            case "DORMITORY":
                successCount = importDormitory(sheet, operatorId, errorMessages);
                break;
            case "ROOM":
                successCount = importRoom(sheet, operatorId, errorMessages);
                break;
            case "EMPLOYEE":
                successCount = importEmployee(sheet, operatorId, errorMessages);
                break;
            case "RESIDENT":
                successCount = importResidentRecord(sheet, operatorId, errorMessages);
                break;

            default:
                throw new RuntimeException("不明なインポート種別: " + importType);
        }

        return new ImportResult(totalCount, successCount);
    }

    /**
     * インポート結果を保持する内部クラス
     */
    private static class ImportResult {
        final int totalCount;
        final int successCount;
        ImportResult(int totalCount, int successCount) {
            this.totalCount = totalCount;
            this.successCount = successCount;
        }
    }

    /**
     * インポートログを更新して結果を返す（トランザクション外）
     */
    private ImportResultResponse updateImportLog(ImportLog importLog, String fileName, String importType,
                                                   int totalCount, int successCount,
                                                   List<String> errorMessages, String operatorId) {
        // エラー詳細をJSON形式で構築
        String errorDetails = null;
        if (!errorMessages.isEmpty()) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < errorMessages.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("{\"row\":").append(i + 2).append(",\"message\":\"").append(errorMessages.get(i)).append("\"}");
            }
            sb.append("]");
            errorDetails = sb.toString();
        }

        int errorCount = totalCount - successCount;
        int status;
        if (errorCount == 0 && totalCount > 0) {
            status = 1; // 成功
        } else if (successCount > 0) {
            status = 2; // 一部失敗
        } else {
            status = 3; // 失敗
        }

        // インポートログを更新
        importLog.setTotalCount(totalCount);
        importLog.setSuccessCount(successCount);
        importLog.setErrorCount(errorCount);
        importLog.setErrorDetails(errorDetails);
        importLog.setImportStatus(status);
        importLog.setUpdateBy(operatorId);
        importLogMapper.update(importLog);

        log.info("Excelインポート完了: total={}, success={}, error={}", totalCount, successCount, errorCount);

        return ImportResultResponse.builder()
                .importLogId(importLog.getId())
                .fileName(fileName)
                .importType(importType)
                .totalCount(totalCount)
                .successCount(successCount)
                .errorCount(errorCount)
                .errorMessages(errorMessages)
                .importStatus(status)
                .build();
    }

    /**
     * 寮マスタをインポート
     */
    private int importDormitory(Sheet sheet, String operatorId, List<String> errors) {
        int success = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            try {
                String dormName = getCellStringValue(row.getCell(0));
                String region = getCellStringValue(row.getCell(1));
                String address = getCellStringValue(row.getCell(2));
                String dormCondition = getCellStringValue(row.getCell(3));
                Integer totalRooms = getCellIntValue(row.getCell(4));

                if (dormName == null || dormName.isBlank()) {
                    errors.add("寮名称が空です (行" + (i + 1) + ")");
                    continue;
                }
                if (region == null || region.isBlank()) {
                    errors.add("地域が空です (行" + (i + 1) + ")");
                    continue;
                }

                Dormitory dorm = Dormitory.builder()
                        .dormName(dormName)
                        .region(region)
                        .address(address)
                        .dormCondition(dormCondition != null ? dormCondition : "ANY")
                        .totalRooms(totalRooms != null ? totalRooms : 0)
                        .status(1)
                        .createBy(operatorId)
                        .build();
                dormitoryMapper.insert(dorm);
                success++;
            } catch (Exception e) {
                errors.add("行" + (i + 1) + ": " + e.getMessage());
            }
        }
        return success;
    }

    /**
     * 部屋情報をインポート
     */
    private int importRoom(Sheet sheet, String operatorId, List<String> errors) {
        int success = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            try {
                Long dormitoryId = getCellLongValue(row.getCell(0));
                String roomNumber = getCellStringValue(row.getCell(1));
                Integer capacity = getCellIntValue(row.getCell(2));
                BigDecimal dailyRate = getCellDecimalValue(row.getCell(3));

                if (dormitoryId == null) {
                    errors.add("寮IDが空です (行" + (i + 1) + ")");
                    continue;
                }
                if (roomNumber == null || roomNumber.isBlank()) {
                    errors.add("部屋番号が空です (行" + (i + 1) + ")");
                    continue;
                }

                Room room = Room.builder()
                        .dormitoryId(dormitoryId)
                        .roomNumber(roomNumber)
                        .capacity(capacity != null ? capacity : 1)
                        .currentOccupancy(0)
                        .dailyRate(dailyRate != null ? dailyRate : BigDecimal.ZERO)
                        .status(1)
                        .createBy(operatorId)
                        .build();
                roomMapper.insert(room);
                success++;
            } catch (Exception e) {
                errors.add("行" + (i + 1) + ": " + e.getMessage());
            }
        }
        return success;
    }

    /**
     * 社員情報をインポート
     */
    private int importEmployee(Sheet sheet, String operatorId, List<String> errors) {
        int success = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            try {
                String empNo = getCellStringValue(row.getCell(0));
                String empName = getCellStringValue(row.getCell(1));
                String gender = getCellStringValue(row.getCell(2));
                String nationality = getCellStringValue(row.getCell(3));
                String phone = getCellStringValue(row.getCell(4));
                String email = getCellStringValue(row.getCell(5));

                if (empNo == null || empNo.isBlank()) {
                    errors.add("社員番号が空です (行" + (i + 1) + ")");
                    continue;
                }
                if (empName == null || empName.isBlank()) {
                    errors.add("氏名が空です (行" + (i + 1) + ")");
                    continue;
                }

                Employee emp = Employee.builder()
                        .empNo(empNo)
                        .empName(empName)
                        .gender(gender != null ? gender : "MALE")
                        .nationality(nationality != null ? nationality : "日本")
                        .phone(phone)
                        .email(email)
                        .hireDate(LocalDate.now())
                        .empStatus(1)
                        .createBy(operatorId)
                        .build();
                employeeMapper.insert(emp);
                success++;
            } catch (Exception e) {
                errors.add("行" + (i + 1) + ": " + e.getMessage());
            }
        }
        return success;
    }

    /**
     * 入居履歴をインポート
     */
    private int importResidentRecord(Sheet sheet, String operatorId, List<String> errors) {
        int success = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            try {
                Long employeeId = getCellLongValue(row.getCell(0));
                Long roomId = getCellLongValue(row.getCell(1));
                LocalDate checkinDate = getCellDateValue(row.getCell(2));
                LocalDate checkoutDate = getCellDateValue(row.getCell(3));
                String notes = getCellStringValue(row.getCell(4));

                if (employeeId == null) {
                    errors.add("社員IDが空です (行" + (i + 1) + ")");
                    continue;
                }
                if (roomId == null) {
                    errors.add("部屋IDが空です (行" + (i + 1) + ")");
                    continue;
                }
                if (checkinDate == null) {
                    errors.add("入居日が空です (行" + (i + 1) + ")");
                    continue;
                }

                ResidentRecord record = ResidentRecord.builder()
                        .employeeId(employeeId)
                        .roomId(roomId)
                        .checkinDate(checkinDate)
                        .checkoutDate(checkoutDate)
                        .isActive(checkoutDate == null ? 1 : 0)
                        .notes(notes)
                        .status(checkoutDate == null ? "CHECKED_IN" : "CHECKED_OUT")
                        .createBy(operatorId)
                        .build();
                residentRecordMapper.insert(record);
                success++;
            } catch (Exception e) {
                errors.add("行" + (i + 1) + ": " + e.getMessage());
            }
        }
        return success;
    }



    // ========== Excel セル読み取りヘルパー ==========

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                yield (val == Math.floor(val)) ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }

    private Integer getCellIntValue(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (int) cell.getNumericCellValue();
                case STRING -> {
                    String val = cell.getStringCellValue().trim();
                    yield val.isEmpty() ? null : Integer.parseInt(val);
                }
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private Long getCellLongValue(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (long) cell.getNumericCellValue();
                case STRING -> {
                    String val = cell.getStringCellValue().trim();
                    yield val.isEmpty() ? null : Long.parseLong(val);
                }
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal getCellDecimalValue(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING -> {
                    String val = cell.getStringCellValue().trim();
                    yield val.isEmpty() ? null : new BigDecimal(val);
                }
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate getCellDateValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }
            if (cell.getCellType() == CellType.STRING) {
                String val = cell.getStringCellValue().trim();
                if (val.isEmpty()) return null;
                return LocalDate.parse(val);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ImportLog findById(Long id) {
        if (id == null) throw new IllegalArgumentException("IDは必須です");
        return importLogMapper.findById(id);
    }

    @Override
    public PageResponse<ImportLog> listImportLogs(ImportLogQueryRequest query) {
        if (query == null) query = ImportLogQueryRequest.builder().build();
        if (query.getPageNum() == null || query.getPageNum() < 1) query.setPageNum(1);
        if (query.getPageSize() == null || query.getPageSize() < 1) query.setPageSize(10);

        int offset = (query.getPageNum() - 1) * query.getPageSize();

        List<ImportLog> list = importLogMapper.findAll(
                query.getImportType(),
                query.getImportStatus(),
                offset,
                query.getPageSize()
        );

        Long total = importLogMapper.countByCondition(
                query.getImportType(),
                query.getImportStatus()
        );

        int totalPage = (int) ((total + query.getPageSize() - 1) / query.getPageSize());

        return PageResponse.<ImportLog>builder()
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .total(total)
                .totalPage(totalPage)
                .list(list)
                .build();
    }
}