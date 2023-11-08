-- 释放锁的Lua脚本
local key = KEYS[1]; -- 锁的key
local threadId = ARGV[1]; -- 线程的唯一标识
local releaseTime = ARGV[2]; -- 锁的自动释放时间
-- 判断当前的锁是否被自己持有
if(redis.call('hexists', key, threadId) == 0) then
    -- 不是自己的则直接返回
    return nil;
end;
-- 是自己的锁，则重入次数 -1
local count = redis.call(('hincrby', key, threadId, -1);
-- 判断是否重入次数是否已经为0
if(count > 0)then
    -- 大于0说明不能释放锁，重置有效期然后返回
    redis.call('expire', key, releaseTime);
    return nil;
else -- 等于0说明可以释放锁，直接删除
    redis.call('del', key);
    return nil;
end