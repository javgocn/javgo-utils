package cn.javgo.utils.distributed;

/**
 * Desc: 分布式锁顶层接口，定义分布式锁的基本操作
 *
 * 策略模式 (Strategy Pattern)：通过 lock 方法的多种重载形式，实现了不同的锁定策略。调用方可以根据需要选择合适的锁定策略。
 *
 * @author javgo
 * @create 2024-08-04 17:40
 */
public interface DistributedLock {

    /**
     * 获取锁
     *
     * @param key 锁的键
     * @return 是否成功获取锁
     */
    boolean lock(String key);

    /**
     * 获取锁
     *
     * @param key 锁的键
     * @param retryTimes 重试次数
     * @return 是否成功获取锁
     */
    boolean lock(String key, int retryTimes);

    /**
     * 获取锁
     *
     * @param key 锁的键
     * @param retryTimes 重试次数
     * @param sleepMillis 每次重试的间隔时间（毫秒）
     * @return 是否成功获取锁
     */
    boolean lock(String key, int retryTimes, long sleepMillis);

    /**
     * 获取锁
     *
     * @param key 锁的键
     * @param expire 锁的过期时间（毫秒）
     * @return 是否成功获取锁
     */
    boolean lock(String key, long expire);

    /**
     * 获取锁
     *
     * @param key 锁的键
     * @param expire 锁的过期时间（毫秒）
     * @param retryTimes 重试次数
     * @return 是否成功获取锁
     */
    boolean lock(String key, long expire, int retryTimes);

    /**
     * 获取锁
     *
     * @param key 锁的键
     * @param expire 锁的过期时间（毫秒）
     * @param retryTimes 重试次数
     * @param sleepMillis 每次重试的间隔时间（毫秒）
     * @return 是否成功获取锁
     */
    boolean lock(String key, long expire, int retryTimes, long sleepMillis);

    /**
     * 释放锁
     *
     * @param key 锁的键
     * @return 是否成功释放锁
     */
    boolean releaseLock(String key);

    /**
     * 获取锁键的前缀
     *
     * @return 锁键的前缀
     */
    String getKeyPrefix();
}
