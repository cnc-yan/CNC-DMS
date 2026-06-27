package com.cnc.dms.controller;

import com.cnc.dms.dto.PageResponse;
import com.cnc.dms.dto.Result;
import com.cnc.dms.dto.RoomCreateRequest;
import com.cnc.dms.dto.RoomQueryRequest;
import com.cnc.dms.dto.RoomUpdateRequest;
import com.cnc.dms.entity.Room;
import com.cnc.dms.service.RoomService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部屋管理 Controller
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * IDで部屋を検索
     */
    @GetMapping("/{id}")
    public Result<Room> getById(@PathVariable Long id) {
        try {
            Room room = roomService.findById(id);
            if (room == null) {
                return Result.fail("部屋が存在しません");
            }
            return Result.success(room);
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }

    /**
     * 条件付きページング検索
     */
    @GetMapping("/list")
    public Result<PageResponse<Room>> listRooms(RoomQueryRequest query) {
        try {
            PageResponse<Room> page = roomService.listRooms(query);
            return Result.success(page);
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }

    /**
     * 某寮に属する部屋一覧を取得
     */
    @GetMapping("/by-dormitory/{dormitoryId}")
    public Result<List<Room>> getByDormitoryId(@PathVariable Long dormitoryId) {
        try {
            List<Room> rooms = roomService.findByDormitoryId(dormitoryId);
            return Result.success(rooms);
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (Exception e) {
            return Result.fail("検索失敗", e.getMessage());
        }
    }

    /**
     * 新規作成
     */
    @PostMapping("")
    public Result<String> create(@RequestBody RoomCreateRequest request) {
        try {
            int rows = roomService.createRoom(request);
            if (rows > 0) {
                return Result.success("成功", "部屋作成成功");
            }
            return Result.fail("部屋作成失敗");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("作成失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("作成失敗", e.getMessage());
        }
    }

    /**
     * 更新
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody RoomUpdateRequest request) {
        try {
            int rows = roomService.updateRoom(request);
            if (rows > 0) {
                return Result.success("成功", "部屋更新成功");
            }
            return Result.fail("部屋更新失敗");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("更新失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("更新失敗", e.getMessage());
        }
    }

    /**
     * 削除
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            int rows = roomService.deleteRoom(id);
            if (rows > 0) {
                return Result.success("成功", "部屋削除成功");
            }
            return Result.fail("部屋削除失敗");
        } catch (IllegalArgumentException e) {
            return Result.fail("パラメータエラー", e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("削除失敗", e.getMessage());
        } catch (Exception e) {
            return Result.fail("削除失敗", e.getMessage());
        }
    }
}
