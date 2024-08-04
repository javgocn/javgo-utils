package cn.javgo.utils.exception;

/**
 * Desc: 自定义用户可见异常类，抛出该类异常的 controller 会被类 MethodMonitor 进行拦截, 并把异常信息抛给用户看
 *
 * @author javgo
 * @create 2024-08-04 22:37
 */
public class UserViewException extends RuntimeException {

    public UserViewException() {
        super();
    }

    public UserViewException(String message) {
        super(message);
    }

    public UserViewException(Throwable cause) {
        super(cause);
    }

    public UserViewException(String message, Throwable cause) {
        super(message, cause);
    }
}
