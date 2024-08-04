package cn.javgo.utils.distributed.impl;

import cn.javgo.utils.distributed.DistributedLock;
import org.springframework.beans.factory.annotation.Value;

/**
 * Desc: 抽象分布式锁实现，提供了分布式锁的基本功能
 * <p>
 * 模板方法模式 (Template Method Pattern)：
 * 在 AbstractDistributedLock 中定义了获取锁的模板方法，具体的锁逻辑由子类 RedisDistributedLockService 实现。可以在不改变算法结构的前提下，
 * 允许子类重新定义算法的某些步骤。
 *
 * @author javgo
 * @create 2024-08-04 17:43
 */
public abstract class AbstractDistributedLock implements DistributedLock {

    /**
     * 默认锁超时时间（毫秒）
     */
    @Value("${lock.timeoutMillis}")
    private long timeoutMillis;

    /**
     * 默认重试次数
     */
    @Value("${lock.retryTimes}")
    private int retryTimes;

    /**
     * 默认每次重试的间隔时间（毫秒）
     */
    @Value("${lock.sleepMillis}")
    private long sleepMillis;

    /**
     * 获取锁
     *
     * @param key 锁的键
     * @return 是否成功获取锁
     */
    @Override
    public boolean lock(String key) {
        return lock(key, timeoutMillis, retryTimes, sleepMillis);
    }

    /**
     * 获取锁
     *
     * @param key        锁的键
     * @param retryTimes 重试次数
     * @return 是否成功获取锁
     */
    @Override
    public boolean lock(String key, int retryTimes) {
        return lock(key, timeoutMillis, retryTimes, sleepMillis);
    }

    /**
     * 获取锁
     *
     * @param key    锁的键
     * @param expire 锁的过期时间（毫秒）
     * @return 是否成功获取锁
     */
    @Override
    public boolean lock(String key, long expire) {
        return lock(key, expire, retryTimes, sleepMillis);
    }

    /**
     * 获取锁
     *
     * @param key         锁的键
     * @param retryTimes  重试次数
     * @param sleepMillis 每次重试的间隔时间（毫秒）
     * @return 是否成功获取锁
     */
    @Override
    public boolean lock(String key, int retryTimes, long sleepMillis) {
        return lock(key, timeoutMillis, retryTimes, sleepMillis);
    }

    /**
     * 获取锁
     *
     * @param key        锁的键
     * @param expire     锁的过期时间（毫秒）
     * @param retryTimes 重试次数
     * @return 是否成功获取锁
     */
    @Override
    public boolean lock(String key, long expire, int retryTimes) {
        return lock(key, expire, retryTimes, sleepMillis);
    }

    @Override
    public String getKeyPrefix() {
        return "";
    }
}
