local key = KEYS[1]
local fansUserId = ARGV[1]
local timestamp = ARGV[2]

local exists = redis.call('EXISTS', key)
-- 若redis无数据则只需落数据库即可
if exists == 0 then
    return -1
end

local size = redis.call('ZCARD', key)
-- 若超5000则删除最早关注的粉丝
if size >= 5000 then
    redis.call('ZPOPMIN', key)
end

-- 添加新粉丝关系
redis.call('ZADD', key, timestamp, fansUserId)
return 0