# AI 编程小助手前端

Vue 3 + Vite 单页聊天室，支持自动生成聊天室 ID，并通过 SSE 流式显示后端回复。

## 本地运行

1. 安装依赖

```bash
npm install
```

2. 启动开发服务

```bash
npm run dev
```

3. 打包构建

```bash
npm run build
```

## 后端配置

默认接口前缀为 `http://localhost:8081/api`。

如果后端地址不同，可以在启动时设置环境变量 `VITE_API_BASE_URL`。

```bash
set VITE_API_BASE_URL=http://localhost:8081/api
npm run dev
```