
-- KEYS[1]:锁的key  ARGV[1]:当前线程标识
-- 比较线程标识与锁中的标识是否一致
if(redis.call('get', KEYS[1]) == ARGV[1]) then
    -- 一致，则删除锁，返回1
    return redis.call('del', KEYS[1])
end
-- 不一致，返回1
return 0