package com.toy.store.exception;

/**
 * 自定義全域異常
 */
public class AppException extends RuntimeException {
    private final String code;

    public AppException(String message) {
        super(message);
        this.code = "ERROR";
    }

    public AppException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
