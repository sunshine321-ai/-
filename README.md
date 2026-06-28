# 卷积核微课平台

这是一个面向卷积核与卷积神经网络入门学习的微课平台，采用前后端分离架构：

- 前端：Vue 3 + Vite，包含微课观看、截图笔记、AI 助教、课后学习、实验工具、数据可视化等页面。
- 后端：Spring Boot + MyBatis，提供学习进度、截图笔记、错题本、书签、闪卡、成就和 AI 调用接口。
- 数据库：MySQL，使用 Flyway 管理表结构。
- AI：DashScope/通义千问，用于智能问答、截图分析、主观题评价和视频章节识别。

## 目录结构

```text
frontend/                              Vue 前端
  public/media/                        微课视频和章节识别结果
  public/legacy/                       课后学习旧版 HTML 页面
  public/pages/                        拓展实验页面
  src/api/                             前端接口封装
  src/views/                           Vue 路由页面

java-backend/microcourse-backend/      Spring Boot 后端
  src/main/java/.../controller/         HTTP 接口
  src/main/java/.../service/            业务接口
  src/main/java/.../service/impl/       业务实现
  src/main/java/.../mapper/             MyBatis 数据访问
  src/main/resources/db/migration/      Flyway 数据库迁移

```

## 启动方式

后端：

```powershell
cd java-backend/microcourse-backend
.\mvnw.cmd spring-boot:run
```

前端：

```powershell
cd frontend
npm install
npm run dev
```

访问地址：

- 前端：http://localhost:5173
- 后端健康检查：http://localhost:8000/api/v1/health

## 环境变量

项目根目录的 `.env` 用于本地配置，包含数据库和 AI 密钥。不要提交到 GitHub。

```dotenv
DASHSCOPE_API_KEY=你的密钥
DASHSCOPE_CHAT_URL=https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
QWEN_MODEL=qwen-plus
QWEN_VISION_MODEL=qwen-vl-plus

MYSQL_URL=jdbc:mysql://localhost:3306/microcourse?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
MYSQL_USER=root
MYSQL_PASSWORD=123456
```

## 运行时文件

- 截图笔记图片保存在 `storage/screenshots` 或后端启动目录下的 `storage/screenshots`。
- 数据库只保存截图图片路径和笔记内容。
- 浏览器本地导出的 Word/PDF 不经过后端保存。
- 视频章节识别会使用浏览器抽帧，并调用后端 AI 接口生成章节目录。

更多接口说明见 (API_CONTRACT.md)，架构说明见 (ARCHITECTURE.md)。
