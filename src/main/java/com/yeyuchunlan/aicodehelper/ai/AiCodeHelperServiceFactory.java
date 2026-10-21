package com.yeyuchunlan.aicodehelper.ai;

import com.yeyuchunlan.aicodehelper.tools.InterviewQuestionTool;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AiCodeHelperService 的装配工厂。
 * 这里注册的组件会作用于接口上的所有方法：模型、记忆、RAG 检索器、本地工具、MCP 工具。
 */
@Configuration
public class AiCodeHelperServiceFactory {

    @Resource
    private ChatModel myQwenChatModel;
    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    // 构造 AI 服务的代理实现，接口里的每个方法都会走到这套配置
    @Bean
    public AiCodeHelperService aiCodeHelperService(McpToolProvider mcpToolProvider,
                                                   ObjectProvider<ContentRetriever> contentRetrieverProvider) {
        AiServices<AiCodeHelperService> builder = AiServices.builder(AiCodeHelperService.class)
                .chatModel(myQwenChatModel)                              // 同步调用的模型
                .streamingChatModel(qwenStreamingChatModel)             // 流式输出的模型
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.withMaxMessages(10))     // 按 @MemoryId 为每个会话维护独立记忆
                .tools(new InterviewQuestionTool())                      // 本地工具：@Tool 注解的方法
                .toolProvider(mcpToolProvider);                          // 远程工具：通过 MCP 协议获取
        // rag.enabled=false 时容器里没有 contentRetriever，这里跳过 RAG，其余能力照常可用
        ContentRetriever contentRetriever = contentRetrieverProvider.getIfAvailable();
        if (contentRetriever != null) {
            builder.contentRetriever(contentRetriever);                  // RAG：每次调用前先检索相关文档
        }
        return builder.build();
    }




}

