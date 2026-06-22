# 卷积核微课平台

项目采用严格的前后端分离结构：Vue 负责所有页面，FastAPI 只提供 API。后续可以在 IDEA 中用 Spring Boot 实现相同的 API，并直接替换 Python 后端。

## 目录

```text
frontend/                 Vue 3 + JavaScript 前端
  public/                 视频及暂未组件化的旧 HTML 页面
  src/views/              路由页面
  src/components/         公共组件
  src/api/                按业务拆分的后端接口
backend/                  纯 Python 后端
  routers/                HTTP 接口
  services/               AI、导出等业务逻辑
  database.py             MySQL 数据访问
  app.py                  FastAPI 应用装配
storage/                  后端运行时生成的截图和导出文件
main.py                   Python 后端启动入口
docs/                     架构及 API 契约
```

## 启动

首次安装：

```powershell
pip install -r requirements.txt
cd frontend
npm install
```

终端一，在项目根目录启动后端：

```powershell
python main.py
```

终端二，启动前端：

```powershell
cd frontend
npm run dev
```

访问地址：

- 前端：http://localhost:5173
- API 文档：http://localhost:8000/docs
- 健康检查：http://localhost:8000/api/v1/health

## 环境变量

`.env` 示例：

```dotenv
DASHSCOPE_API_KEY=你的密钥
QWEN_MODEL=qwen-plus
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_USER=root
MYSQL_PASSWORD=你的密码
MYSQL_DB=vlab
ALLOWED_ORIGINS=http://localhost:5173
```

详细边界见 [架构说明](docs/ARCHITECTURE.md)，Java 重写时遵循 [API 契约](docs/API_CONTRACT.md)。
