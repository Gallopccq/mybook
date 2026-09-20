note模块，负责笔记相关的各种服务：
笔记详情：
笔记添加：
笔记删除：
笔记修改：
笔记仅自己可见：
笔记置顶

笔记添加接口：
/note/publish
```json
{
  "id": 100, 
  "title": "测试笔记标题",
  "visible": 1,
  "top": 1,
  "content": "这是一条测试笔记",
  "backgroundImg": "http://123.top",
  "video": "http://123.top",
  "type": 1
}
```
/note/delete
```json
{
  "noteId": 101
}
```
/note/detail
```json
{
  "noteId": 101
}
```
/note/update
```json
{
  "noteId": 101,
  "content": "新测试笔记内容"
}
```
/note/visible/onlyme
```json
{
  "noteId": 101,
  "visible": 1
}
```
/note/top
```json
{
  "noteId": 101,
  "top": 1
}
```