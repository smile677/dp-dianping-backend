package com.smile67.utils;

/**
 * @author smile67~
 */
public interface ILock {
    /**
     * 尝试获取锁
     *
     * @param timeoutSec 锁持有的超时时间，过期后自动释放，作为兜底，防止发生死锁
     * @return ture:代表获取锁成功 false:代表获取锁失败
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
}
