package com.cnc.dms.exception;

/**
 * 楽観ロック例外
 * 同時更新によるバージョン競合が発生した場合にスローする
 */
public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException(String message) {
        super(message);
    }

    public OptimisticLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
