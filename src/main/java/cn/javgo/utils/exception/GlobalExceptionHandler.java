package cn.javgo.utils.exception;

import cn.javgo.utils.common.ApiResponse;
import cn.javgo.utils.common.enums.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Desc: 全局异常处理器, 捕获并处理所有未处理的异常。
 *
 * @author javgo
 * @create 2024-08-04 22:08
 */
@Slf4j
@RestControllerAdvice // 标识为全局异常处理器
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ApiResponse<Object> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage());
        return ApiResponse.fail(StatusCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ApiResponse<Object> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage());
        return ApiResponse.fail(StatusCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
