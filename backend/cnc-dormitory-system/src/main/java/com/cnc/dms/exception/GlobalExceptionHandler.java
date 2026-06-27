package com.cnc.dms.exception;

import com.cnc.dms.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * グローバル例外ハンドラ
 * 全Controllerで発生した例外を統一的に処理する
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 業務例外を処理
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e) {
        logger.warn("Business exception: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    /**
     * リソース未検出例外を処理
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleResourceNotFound(ResourceNotFoundException e) {
        logger.warn("Resource not found: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    /**
     * 楽観ロック例外を処理
     */
    @ExceptionHandler(OptimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleOptimisticLock(OptimisticLockException e) {
        logger.warn("Optimistic lock conflict: {}", e.getMessage());
        return Result.fail("同時更新による競合が発生しました。再試行してください。");
    }

    /**
     * パラメータ不正例外を処理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        logger.warn("Illegal argument: {}", e.getMessage());
        return Result.fail("パラメータエラー", e.getMessage());
    }

    /**
     * その他予期せぬ例外を処理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        logger.error("Unexpected error: ", e);
        return Result.fail("サーバー内部エラーが発生しました");
    }
}
