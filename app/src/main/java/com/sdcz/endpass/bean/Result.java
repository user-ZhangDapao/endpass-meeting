package com.sdcz.endpass.bean;

public class Result<T> {

    public static final int SUCCESS_CODE = 0;
    public static final int SUCCESS_EMPTY = 1;
    private int code;
    private String errorMsg;
    private T data;

    public int getCode() {
        return code;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public T getData() {
        return data;
    }

    public static <T> Result<T> createSuccess(T t) {
        Result<T> result = new Result<T>();
        result.code = SUCCESS_CODE;
        result.errorMsg = "";
        result.data = t;
        return result;
    }

    public static <T> Result<T> createError(int code, String errorMsg) {
        Result<T> result = new Result<T>();
        result.code = code;
        result.errorMsg = errorMsg;
        return result;
    }

    public static <T> Result<T> createEmpty() {
        Result<T> result = new Result<T>();
        result.code = SUCCESS_EMPTY;
        result.errorMsg = "result is empty!";
        return result;
    }
}
