package com.yeyuchunlan.aicodehelper.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 客户端配置：连接智谱托管的联网搜索服务。
 * 分三层：Transport 负责传输 → McpClient 负责协议 → McpToolProvider 把远程工具交给 AI 服务。
 * 注意：这里只是 MCP 的调用方，MCP 服务本身由第三方提供。
 */
@Configuration
public class McpConfig {

    @Value("${bigmodel.api-key}")
    private String apiKey;

    @Bean
    public McpToolProvider mcpToolProvider() {
        // 1. 传输层：用 HTTP SSE 连接远程 MCP 服务，鉴权参数直接拼在 URL 里
        McpTransport transport = new HttpMcpTransport.Builder()
                .sseUrl("https://open.bigmodel.cn/api/mcp/web_search/sse?Authorization=" + apiKey)
                .logRequests(true) // 打印请求日志
                .logResponses(true) // 打印响应日志
                .build();
        // 2. 客户端：负责与 MCP 服务完成握手和协议交互
        McpClient mcpClient = new DefaultMcpClient.Builder()
                .key("yupiMcpClient") // 客户端标识
                .transport(transport)
                .build();
        // 3. 工具提供者：把该客户端暴露的远程工具打包，交给 AiServices 使用
        McpToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClient)
                .build();
        return toolProvider;
    }
}

