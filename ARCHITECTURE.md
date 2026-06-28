# 项目架构

```text
浏览器
  │
  ▼
Vue 3 前端（localhost:5173）
  ├─ views/       页面
  ├─ api/         后端接口函数和浏览器本地导出
  ├─ config/      API 基础地址
  └─ public/      视频和旧教学页面
  │ /api/v1/*
  ▼
Spring Boot 后端（localhost:8000）
  ├─ controller/  接收 HTTP 请求
  ├─ service/     AI 等业务接口
  ├─ service/impl 数据库业务实现
  ├─ mapper/      MyBatis 数据访问
  ├─ pojo/        实体和请求对象
  └─ config/      文件访问与异常处理
  ├──────────────► MySQL
  ├──────────────► DashScope（通义千问）
  └──────────────► storage（截图文件）
```

## 数据保存位置

- MySQL：学习笔记、截图信息、错题、进度、书签和聊天记录。
- `storage/screenshots`：截图图片文件；MySQL 只保存图片网址。
- 浏览器下载/打印：学习资料导出，不经过后端存储。
- 浏览器中的 Vue `ref`：仅用于当前页面显示，不承担持久化。

## 调用规则

1. Vue 页面只调用 `frontend/src/api` 中的函数。
2. API 文件请求 `/api/v1` 下的 Java Controller。
3. 数据库业务依次经过 Controller → Service → Mapper → MySQL。
4. MySQL 密码和 DashScope 密钥只配置在后端环境中。
5. 当前尚未实现登录，因此所有接口暂时使用默认用户 `user_id=1`。

完整接口见 [API_CONTRACT.md](API_CONTRACT.md)。
