# AI 编程小助手

一个面向编程学习与求职面试的 AI 问答应用：支持多轮对话、流式输出、RAG 知识库检索、本地工具调用和 MCP 联网搜索。

## 功能

- **多轮对话**：按会话 ID 隔离记忆，窗口保留最近 10 条消息
- **流式输出**：SSE 逐字推送，前端用 EventSource 接收
- **RAG 知识库**：启动时加载 docs 下的 Markdown，切分后向量化，检索 topK=5、相似度阈值 0.75
- **本地工具**：`interviewQuestionSearch`，按关键词从面试鸭检索面试题
- **MCP 联网搜索**：接入智谱托管的 web_search MCP 服务
- **输入护栏**：请求进入模型前做敏感词校验
- **可观测**：ChatModelListener 记录模型请求、响应和异常（DEBUG 级别）

## 技术栈

| 层 | 技术 |
|---|---|
| 语言 / 框架 | Java 21、Spring Boot 3.5 |
| AI 框架 | LangChain4j 1.1.0 |
| 对话模型 | 通义千问 `qwen3.8-27b`（百炼 OpenAI 兼容接口） |
| 向量模型 | `qwen3.7-text-embedding-flash`（DashScope 原生接口） |
| 向量存储 | InMemoryEmbeddingStore |
| 工具协议 | MCP（SSE 传输） |
| 前端 | Vue 3 + Vite 6，原生 CSS |
| Markdown 渲染 | marked + DOMPurify |

## 架构

```text
浏览器 (Vue 3)
  │  EventSource / SSE
  ▼
AiController  GET /api/ai/chat
  │
  ▼
AiCodeHelperService（接口，由 AiServices 生成代理）
  ├─ InputGuardrail         输入校验
  ├─ ChatMemory             按 memoryId 隔离的多轮记忆
  ├─ ContentRetriever       RAG 检索（InMemoryEmbeddingStore）
  ├─ InterviewQuestionTool  本地工具
  └─ McpToolProvider        MCP 远程工具
  │
  ▼
qwen3.8-27b（OpenAI 兼容接口）
```

启动时 `RagConfig` 会从 classpath 读取 `docs` 下的 Markdown，按 1000 字切分、200 字重叠，把文件名拼到每个片段前，再向量化写入内存向量库。

## 目录结构

```text
src/main/java/com/yeyuchunlan/aicodehelper
├── ai/           AI 服务接口与装配
├── config/       跨域配置
├── controller/   HTTP 接口
├── guardrail/    输入护栏
├── listener/     模型调用监听
├── mcp/          MCP 客户端配置
├── model/        OpenAI 兼容模型配置
├── rag/          RAG 配置
└── tools/        本地工具

src/main/resources
├── docs/         知识库文档
├── application.yml
└── system-prompt.txt

frontend/         Vue 3 前端
```

## 快速开始

### 环境

- JDK 21
- Node.js 18+
- Maven（项目自带 mvnw）

### 配置

在 `src/main/resources/` 下创建 `application-local.yml`（已被 .gitignore 忽略）：

```yaml
ai:
  base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
  api-key: <百炼 API Key>
  chat-model: qwen3.8-27b
  streaming-chat-model: qwen3.8-27b

langchain4j:
  community:
    dashscope:
      embedding-model:
        model-name: qwen3.7-text-embedding-flash
        api-key: <百炼 API Key>

bigmodel:
  api-key: <智谱 API Key>

rag:
  enabled: true

app:
  cors:
    allowed-origins: http://localhost:5173
```

- `rag.enabled=false` 时不加载知识库，也不消耗 embedding 额度
- 百炼的 Key 需要在**北京地域**创建，SDK 默认连接北京端点

### 启动后端

```bash
./mvnw spring-boot:run
```

看到 `Started AiCodeHelperApplication` 即启动成功，接口地址 `http://localhost:8081/api`。

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`。

## 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/ai/chat?memoryId=1&message=...` | SSE 流式对话 |

## 测试

```bash
./mvnw test
```

`SafeInputGuardrailTest` 不依赖外部服务，可直接运行；`AiCodeHelperApplicationTests` 需要真实 API Key，默认 `@Disabled`，按需手动运行。

## 已知限制

- 向量库是内存实现，重启后需要重新向量化
- 对话记忆保存在内存中，重启即丢失
- 未做用户体系和鉴权，接口默认只适合本地运行
- 联网搜索依赖第三方 MCP 服务，需要其账户有可用额度
- 输入护栏是演示级关键词校验，不是完整的内容安全方案

## 说明

本项目是一个 AI 应用实践项目，重点在 LangChain4j 的 RAG、工具调用与 MCP 工程装配。知识库文档参考了公开的编程学习与求职资料。
