package cn.javgo.utils.distributed.aspect;

import cn.javgo.utils.common.ApiResponse;
import cn.javgo.utils.common.enums.StatusCode;
import cn.javgo.utils.distributed.DistributedLock;
import cn.javgo.utils.distributed.anno.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Desc: 切面类，用于拦截标记了 RedisLock 注解的方法。在方法执行前后进行锁的获取和释放操作，通过 AOP 切面技术透明地为方法添加分布式锁功能。
 *
 * @author javgo
 * @create 2024-08-04 21:51
 */
@Slf4j
@Aspect // 标识为切面类
@Component // 标识为组件类
public class RedisLockSupport {

    @Autowired
    private DistributedLock redisDistributedLockService;

    /**
     * 拦截标记了 RedisLock 注解的方法，在方法执行前后进行锁的获取和释放操作。
     * @param pjp 切点
     * @param redisLock RedisLock 注解
     * @return 方法执行结果
     * @throws Throwable 抛出的异常
     */
    @Around("@annotation(redisLock)")
    public Object around(ProceedingJoinPoint pjp, RedisLock redisLock) throws Throwable {
        // 获取方法签名
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        // 获锁的 key
        String key = getKey(method, redisLock);

        // 获取重试次数
        int retryTimes = redisLock.action().equals(RedisLock.LockFailAction.CONTINUE) ? redisLock.retryTimes() : 0;
        // 尝试获取锁
        boolean lock = redisDistributedLockService.lock(key, redisLock.keepMills(), retryTimes, redisLock.sleepMills());
        log.info("线程{}获取分布式锁{},获取结果:{}", Thread.currentThread().getId(), key, (lock ? " success" : " failed"));

        // 如果获取锁失败
        if (!lock) {
            // 获取方法返回类型
            Class<?> returnType = method.getReturnType();
            // 根据方法的返回类型，返回相应的失败消息
            if (returnType == ApiResponse.class) {
                return ApiResponse.fail(StatusCode.LOCK_FAIL, redisLock.failMsg());
            } else {
                // 如果方法返回类型不是 ApiResponse，则通过反射修改返回类型为 ApiResponse，并返回失败消息
                return returnType.getDeclaredConstructor(String.class).newInstance(redisLock.failMsg());
            }
        }

        // 得到锁后执行方法，在方法执行完成后释放锁
        try {
            return pjp.proceed();
        } catch (Exception e) {
            log.error("线程{}执行分布式方法{}发生异常:{}", Thread.currentThread().getId(), method.getName(), e.getMessage());
            throw e;
        } finally {
            boolean releaseResult = redisDistributedLockService.releaseLock(key);
            log.info("线程{}释放分布式锁{}，释放结果:{}", Thread.currentThread().getId(), key, (releaseResult ? " success" : " failed"));
        }
    }

    /**
     * 根据方法和注解配置生成锁的key。
     * @param method 方法
     * @param redisLock RedisLock 注解
     * @return 生成的锁key
     */
    private String getKey(Method method, RedisLock redisLock) {
        // 根据注解配置生成锁key(这里可以根据实际业务情况灵活增加 RedisLock 注解中的属性，然后在这里灵活定制 key)
        return redisLock.lockKey();
    }
}
