user relation
负责用户关注，取关。

对应界面为关注列表，关注按钮，取关按钮，关注总数

代码逻辑：
关注按钮：发送包含【用户-关注者】信息，数据库落库，redis缓存。
取关按钮：发送【用户-关注者】信息，数据库删除数据（逻辑删除），redis删缓存。
关注列表：频繁读取，需redis做类似排行榜的功能（ZSET），按关注时间排序，然后异步落库。
关注用户简介：用户头像，用户昵称，用户简介。在redis中存储用户信息，查询用户关注列表后一一查询拼接。

接口：
/relation/follow：
```json
{ 
  "id": 100,
  "followerId": 200 
  
}
```
/relation/unfollow：
```json
{
  "id": 100,
  "followerId": 200
}
```
/relation/following/list:
```json
{
  "id": 100
}
```
/relation/fans/list:
```json
{
  "userId": 27,
  "pageNo": 1
}
```
```json
{
  "success": true,
  "message": null,
  "errorCode": null,
  "data": [
    {
      "userId": 101, // 用户ID
      "avatar": "http://127.0.0.1:9000/xiaohashu/f22e21fb0c144c088bd20bc616916ff3.jpg", // 头像
      "nickname": "犬小哈", // 昵称
      "fansTotal": 0, // 粉丝总量
      "noteTotal": 0 // 笔记总量
    },
    {
      "userId": 28,
      "avatar": null,
      "nickname": "小红薯10100",
      "fansTotal": 0,
      "noteTotal": 0
    }
  ],
  "pageNo": 1,
  "totalCount": 3,
  "pageSize": 10,
  "totalPage": 1
}
```
