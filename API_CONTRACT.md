# Java 后端 API 接口

基础地址：`http://localhost:8000/api/v1`

除 AI 和导出兼容接口外，统一响应格式为：

```json
{ "success": true, "code": 1, "msg": "success", "data": {} }
```

## 系统

- `GET /health`：检查 Java、MySQL 和 AI 密钥配置状态。

## 学习笔记

- `GET /notes`：读取学习笔记。
- `PUT /notes`：保存学习笔记，请求体：`{ "content": "笔记内容" }`。

## 截图笔记

- `GET /screenshots`：读取全部截图。
- `POST /screenshots`：保存截图，请求体：`{ "image": "data:image/png;base64,...", "videoTime": 12.5 }`。
- `PUT /screenshots/{id}`：保存截图笔记和 AI 分析，请求体：`{ "note": "...", "aiAnalysis": "..." }`。
- `DELETE /screenshots/{id}`：删除截图数据库记录和图片文件。
- `POST /screenshots/analyze`：AI 分析截图，请求体：`{ "imagePath": "/files/screenshots/xxx.png" }`。

## 错题本

- `GET /wrong-questions`：读取错题。
- `POST /wrong-questions`：新增错题。
- `PUT /wrong-questions/{id}`：修改错题。
- `DELETE /wrong-questions/{id}`：删除一条错题。
- `DELETE /wrong-questions`：清空错题本。

新增和修改的请求体：

```json
{
  "question": "题目",
  "optionsJson": "[\"A. 选项一\",\"B. 选项二\"]",
  "correctAnswer": "正确答案",
  "userAnswer": "用户答案",
  "note": "解析"
}
```

## 学习进度

- `GET /progress`：读取各章节进度。
- `PUT /progress`：新增或覆盖章节进度。
- `DELETE /progress`：重置全部进度。

```json
{
  "chapterKey": "exercises",
  "progress": 60,
  "completed": true,
  "detailJson": "{\"answers\":{\"1\":\"B\"},\"scores\":[10,null,null,null,null]}",
  "durationSeconds": 125
}
```

`detailJson` 保存模块的详细学习状态；`durationSeconds` 保存累计学习秒数。

## 全站模块使用统计

- `POST /usage/events`：记录一次页面访问或一段实际使用时长。
- `GET /usage/summary`：按模块汇总累计访问次数、累计时长，并返回近 7 天趋势。

```json
{
  "moduleKey": "calculator",
  "eventType": "duration",
  "durationSeconds": 42
}
```

`moduleKey` 可为 `video`、`study`、`ai_tutor`、`playground`、`showcase_3d`、`data_viz`、`calculator`；`eventType` 可为 `page_view` 或 `duration`。数据保存在 `module_usage_events`，与只记录课后学习状态的 `learning_progress` 相互独立。

## 视频书签

- `GET /bookmarks`：读取书签。
- `POST /bookmarks`：新增书签，请求体：`{ "videoTime": 35.5, "label": "重点" }`。
- `DELETE /bookmarks/{id}`：删除一条书签。
- `DELETE /bookmarks`：清空书签。

## 闪卡掌握进度

- `GET /flashcards`：读取已掌握的闪卡序号，例如 `[0, 3, 7]`。
- `PUT /flashcards/{cardIndex}`：将指定闪卡标记为已掌握，序号范围为 `0-9`。
- `DELETE /flashcards`：重置全部闪卡掌握进度。

掌握状态保存在 MySQL 表 `flashcard_progress`，不再使用浏览器 `localStorage`。

## 成就徽章

- `GET /achievements`：读取当前用户已解锁的徽章。
- `PUT /achievements/{achievementKey}`：解锁一项普通徽章。

徽章保存在 MySQL 表 `user_achievements`。接口通过唯一键避免重复记录；11 项普通徽章全部解锁后，后端自动解锁 `master`（卷积大师）。

## 聊天与 AI

- `GET /chats/{context}`：读取聊天记录，`context` 为 `home`、`study` 或 `tutor`。
- `PUT /chats`：整体保存聊天记录。
- `DELETE /chats/{context}`：清空指定场景聊天记录。
- `POST /ai/chat`：调用 AI；Java 会自动把问题和回答写入 `chat_messages`。

```json
{ "message": "什么是卷积核？", "context": "study", "systemPrompt": "可选提示词" }
```

AI 需要设置环境变量 `DASHSCOPE_API_KEY`。

## 导出

导出由浏览器本地完成，不调用后端接口。前端可导出学习笔记、错题或组合资料；Word 会下载可由 Word 打开的 `.doc` 文件，PDF 会打开打印页面，由用户选择“另存为 PDF”。
