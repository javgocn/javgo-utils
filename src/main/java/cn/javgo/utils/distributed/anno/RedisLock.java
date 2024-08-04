package cn.javgo.utils.distributed.anno;

import java.lang.annotation.*;

/**
 * Desc: RedisLock 注解，用于标记需要使用分布式锁的方法。可以通过注解参数灵活配置锁的资源、持锁时间、失败消息、重试次数、重试间隔等。
 *
 * @author javgo
 * @create 2024-08-04 21:47
 */
@Target({ElementType.METHOD}) // 注解作用在方法上
@Retention(RetentionPolicy.RUNTIME) // 注解保留在运行时
@Inherited // 子类可以继承父类的注解
public @interface RedisLock {

    /**
     * 锁的资源，redis的key。
     * @return 锁的资源key
     */
    String lockKey() default "";

    /**
     * 持锁时间,单位毫秒。(默认30秒)
     * @return 持锁时间
     */
    long keepMills() default 30000;

    /**
     * 重试的间隔时间,单位毫秒。(默认500毫秒)
     * 设置为 GIVEUP 时忽略此项。
     * @return 重试间隔时间
     */
    long sleepMills() default 500;

    /**
     * 重试次数。(默认3次)
     * @return 重试次数
     */
    int retryTimes() default 3;

    /**
     * 获取锁失败返回的消息。
     * 返回值为ResultDTO时填充
     * @return 失败消息
     */
    String failMsg() default "获取分布式锁失败";

    /**
     * 当获取失败时候的动作。(默认继续执行)
     * @return 失败时的动作
     */
    LockFailAction action() default LockFailAction.CONTINUE;

    /**
     * 获取锁失败时的动作枚举。
     */
    enum LockFailAction {
        GIVEUP, CONTINUE;
    }
}
