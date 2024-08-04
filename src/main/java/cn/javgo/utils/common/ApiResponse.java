package cn.javgo.utils.common;

import cn.javgo.utils.common.enums.StatusCode;

/**
 * Desc: 通用响应类, 用于封装API的响应数据。
 *
 * @author javgo
 * @create 2024-08-04 22:05
 */
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(StatusCode.SUCCESS.getCode(), StatusCode.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(StatusCode.SUCCESS.getCode(), StatusCode.SUCCESS.getMessage());
    }

    public static <T> ApiResponse<T> fail(StatusCode statusCode) {
        return new ApiResponse<>(statusCode.getCode(), statusCode.getMessage());
    }

    public static <T> ApiResponse<T> fail(StatusCode statusCode, String message) {
        return new ApiResponse<>(statusCode.getCode(), message);
    }

    // Getters and Setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
