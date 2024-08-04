package cn.javgo.utils.common.enums;

/**
 * Desc: 状态码枚举, 用于表示不同的API响应状态码和消息。
 *
 * @author javgo
 * @create 2024-08-04 22:07
 */
public enum StatusCode {

    // 基础异常
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未经授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源未找到"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    // 业务异常
    LOCK_FAIL(5000, "获取分布式锁失败"),
    LOCK_FAIL_GET_LOCK_TIMEOUT(5002, "获取分布式锁失败, 获取锁超时"),

    // 自定义用户可见的请求失败异常
    USER_VIEW_FAIL(1000, "用户可见的请求失败");


    private final int code;
    private final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
