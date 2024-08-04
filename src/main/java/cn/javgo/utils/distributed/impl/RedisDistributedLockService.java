package cn.javgo.utils.distributed.impl;

import cn.javgo.utils.distributed.constans.RedisKeyPrefix;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.commands.JedisCommands;
import redis.clients.jedis.params.SetParams;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Desc: 基于 Redis 的分布式锁服务实现
 * TIP：下面的日志级别根据实际情况调整
 *
 * @author javgo
 * @create 2024-08-04 17:52
 */
@Slf4j
@Service
public class RedisDistributedLockService extends AbstractDistributedLock {

    /**
     * 保存每个线程持有的锁的唯一标识符（UUID）。在释放锁时，通过 ThreadLocal 获取当前线程持有的锁标识，以确保只有持有该锁的线程才能释放锁，从而避免误删其他线程的锁。
     * 1. 当获取锁成功时，将生成的 UUID 存入 ThreadLocal。
     * 2. 释放锁时，从 ThreadLocal 中获取并校验锁标识，确保只有持有锁的线程才能执行释放操作。
     */
    private ThreadLocal<LinkedList<String>> lockFlagStack = ThreadLocal.withInitial(LinkedList::new);

    /**
     * 用于记录需要续期锁的定时任务。
     */
    private ThreadLocal<ScheduledFuture<?>> renewalFuture = new ThreadLocal<>();

    /**
     * 官方提供的 Lua 解锁脚本, 确保脚本的原子性，以避免由于锁过期导致的误删其他线程持有的锁。
     */
    private static final String UNLOCK_LUA_SCRIPT;

    /**
     * Lua 脚本说明：
     * 1. redis.call("get", KEYS[1]) == ARGV[1]：检查键 KEYS[1]（即锁的 key）对应的值是否等于 ARGV[1]（即锁的 UUID）。
     * 2. redis.call("del", KEYS[1])：如果上述检查通过，删除该键（即释放锁）。
     * 3. return 0：如果上述检查未通过，返回 0，表示未能释放锁。
     */
    static {
        UNLOCK_LUA_SCRIPT = "if redis.call(\"get\",KEYS[1]) == ARGV[1] " +
                "then " +
                "    return redis.call(\"del\",KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end ";
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 定时任务线程池
     */
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * 锁的加锁过程如下：
     * 1. 调用 lock 方法检查是否可以直接获取锁, 如果获取失败，根据重试次数和间隔时间进行重试。
     * 2. 调用 setRedisLock 方法使用 Redis 的 SET 命令尝试设置锁。将锁的键、值（UUID）和过期时间传递给 Redis，使用 NX 参数确保只有当键不存在时才能设置，使用 PX 参数设置过期时间。
     *
     * @param key         锁的键
     * @param expire      锁的过期时间（毫秒）
     * @param retryTimes  重试次数
     * @param sleepMillis 每次重试的间隔时间（毫秒）
     * @return 是否获取到锁
     */
    @Override
    public boolean lock(String key, long expire, int retryTimes, long sleepMillis) {
        long startTime = System.nanoTime();
        // 尝试获取锁
        boolean result = setRedisLock(key, expire);

        // 如果获取锁失败，按照传入的重试次数进行重试
        while ((!result) && retryTimes-- > 0) {
            try {
                log.debug("Thread {} failed to acquire lock {}, retrying... remaining retries: {}", Thread.currentThread().getId(), key, retryTimes);
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                log.error("Thread {} interrupted while retrying to acquire lock {}", Thread.currentThread().getId(), key, e);
                Thread.currentThread().interrupt(); // 恢复中断状态
                return false;
            }
            result = setRedisLock(key, expire);
        }

        long endTime = System.nanoTime();
        if (result) {
            log.info("Thread {} acquired lock {} in {} ms", Thread.currentThread().getId(), key, (endTime - startTime) / 1_000_000);
            // 加锁成功，开启定时任务，定时续期锁
            startRenewalTask(key, expire);
        } else {
            log.warn("Thread {} failed to acquire lock {} after {} retries in {} ms", Thread.currentThread().getId(), key, retryTimes, (endTime - startTime) / 1_000_000);
            // 回退逻辑
            handleLockFailure(key);
        }
        return result;
    }

    /**
     * 设置 Redis 分布式锁
     *
     * @param key 锁的键名
     * @param expire 锁的超时时间，单位为毫秒
     * @return 如果成功设置锁，则返回true；否则返回false
     */
    private boolean setRedisLock(String key, long expire) {
        try {
            // 尝试设置 Redis 锁，并返回操作结果
            String result = redisTemplate.execute((RedisCallback<String>) connection -> {
                JedisCommands commands = (JedisCommands) connection.getNativeConnection();
                String uuid = UUID.randomUUID().toString();
                lockFlagStack.get().push(uuid);

                // 使用 NX（Not eXist）和 PX（eXpire）参数设置锁
                // NX 确保锁不存在时才设置，PX 设置锁的过期时间为 expire 毫秒
                SetParams params = new SetParams().nx().px(expire);
                return commands.set(getKeyPrefix() + key, uuid, params);
            });

            // 判断是否成功设置锁
            boolean success = !StringUtils.isEmpty(result);
            if (success) {
                log.debug("Thread {} set lock {} with expire time {} ms", Thread.currentThread().getId(), key, expire);
            } else {
                log.debug("Thread {} failed to set lock {}", Thread.currentThread().getId(), key);
            }
            return success;
        } catch (Exception e) {
            log.error("Thread {} failed to set lock {} due to unexpected error", Thread.currentThread().getId(), key, e);
        }
        // 如果执行到此处，设置锁失败
        return false;
    }


    /**
     * 启动锁续期任务：当线程获得锁后，需要定期续期以保持锁的有效性此方法启动一个定时任务，以确保锁在过期前被续期。
     *
     * @param key 锁的唯一标识符
     * @param expire 锁的过期时间，以毫秒为单位
     */
    private void startRenewalTask(String key, long expire) {
        // 创建一个 Runnable 任务，用于锁的自动续期
        Runnable renewalTask = () -> {
            // 获取当前线程的锁值
            String lockValue = lockFlagStack.get().peek();
            // 检查锁值是否有效
            if (!StringUtils.isEmpty(lockValue)) {
                // 尝试续期锁
                boolean success = renewLock(key, lockValue, expire);
                if (success) {
                    log.info("Thread {} renewed lock {} for {} ms", Thread.currentThread().getId(), key, expire);
                } else {
                    log.warn("Thread {} failed to renew lock {}", Thread.currentThread().getId(), key);
                }
            }
        };
        // 安排一个定时执行的续期任务，首次延时和后续续期间隔为锁过期时间的一半
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(renewalTask, expire / 2, expire / 2, TimeUnit.MILLISECONDS);
        // 保存续期任务的未来引用，以便将来可能的取消操作
        renewalFuture.set(future);
    }


    /**
     * 续租锁，该方法会检查当前线程持有的锁标识是否仍然有效，如果有效，则续期锁的过期时间。
     *
     * @param key 锁的唯一标识
     * @param value 锁的值，用于验证锁的所有权
     * @param expire 锁的过期时间，单位为毫秒
     * @return 续租是否成功
     */
    private boolean renewLock(String key, String value, long expire) {
        try {
            // 使用 RedisTemplate 执行 Redis 操作
            String result = redisTemplate.execute((RedisCallback<String>) connection -> {
                // 获取 Jedis 连接
                JedisCommands commands = (JedisCommands) connection.getNativeConnection();
                // 检查并更新锁
                if (value.equals(commands.get(getKeyPrefix() + key))) {
                    // 设置锁的参数，包括仅当锁存在时更新和设置过期时间
                    SetParams params = new SetParams().xx().px(expire);
                    // 更新锁的过期时间
                    return commands.set(getKeyPrefix() + key, value, params);
                }
                // 如果锁已被其他人持有，则不进行更新
                return null;
            });
            // 检查续租操作的结果
            return !StringUtils.isEmpty(result);
        } catch (Exception e) {
            log.error("Thread {} failed to renew lock {} due to unexpected error", Thread.currentThread().getId(), key, e);
        }
        // 续租失败返回 false
        return false;
    }

    private void handleLockFailure(String key) {
        // 这里可以添加回退逻辑，例如返回默认值或抛出自定义异常
        throw new RuntimeException("Failed to acquire lock: " + key);
    }

    /**
     * 锁的释放过程如下：
     * 1. 调用 releaseLock 方法从 ThreadLocal 中获取当前线程持有的锁标识（UUID）,如果获取不到锁标识，说明当前线程没有持有锁，直接返回 false。
     * 2. 调用 Lua 脚本检查并释放锁，确保操作的原子性。如果 Lua 脚本执行成功，返回 true；否则，返回 false。
     *
     * @param key 锁的键
     * @return 是否释放锁
     */
    @Override
    public boolean releaseLock(String key) {
        // 停止续订任务
        stopRenewalTask();
        long startTime = System.nanoTime();
        try {
            // 获取锁标识并移除
            String releaseValue = lockFlagStack.get().poll();
            if (StringUtils.isEmpty(releaseValue)) {
                log.warn("Thread {} has no lock flag to release for key {}", Thread.currentThread().getId(), key);
                return false;
            }

            // key
            List<String> keys = new ArrayList<>();
            keys.add(getKeyPrefix() + key);

            // value
            List<String> args = new ArrayList<>();
            args.add(releaseValue);

            // 执行 Lua 脚本原子释放锁
            Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
                JedisCommands commands = (JedisCommands) connection.getNativeConnection();
                return (Long) commands.eval(UNLOCK_LUA_SCRIPT, keys, args);
            });
            boolean success = result != null && result > 0;

            long endTime = System.nanoTime();
            if (success) {
                log.info("Thread {} successfully released lock {} in {} ms", Thread.currentThread().getId(), key, (endTime - startTime) / 1_000_000);
            } else {
                log.error("Thread {} failed to release lock {} in {} ms", Thread.currentThread().getId(), key, (endTime - startTime) / 1_000_000);
            }
            return success;
        } catch (Exception e) {
            log.error("Thread {} failed to release lock {} due to unexpected error", Thread.currentThread().getId(), key, e);
        }
        return false;
    }

    /**
     * 用于停止续期任务。在锁被释放后，停止续期任务，避免不必要的资源开销。
     */
    private void stopRenewalTask() {
        // 获取已计划的任务
        ScheduledFuture<?> future = renewalFuture.get();
        // 如果任务存在，则取消任务的执行
        if (future != null) {
            future.cancel(false);
            // 从存储中移除已取消的任务
            renewalFuture.remove();
        }
    }

    @Override
    public String getKeyPrefix() {
        return RedisKeyPrefix.LOCK_KEY;
    }
}
