package com.yeyuchunlan.aicodehelper.model;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 通过 OpenAI 兼容协议接入百炼模型。
 * base-url 指向百炼的 compatible-mode 接口，这样可以使用只提供兼容接口的新模型。
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class OpenAiModelConfig {

    private String baseUrl;              // 兼容接口地址，需以 /v1 结尾

    private String apiKey;               // 百炼 API Key

    private String chatModel;            // 同步对话模型名

    private String streamingChatModel;   // 流式对话模型名

    @Resource
    private ChatModelListener chatModelListener;

    // 同步模型：AiServices 的普通调用走这里
    @Bean
    public ChatModel myQwenChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(chatModel)
                .listeners(List.of(chatModelListener))
                .build();
    }

    // 流式模型：SSE 输出走这里
    @Bean
    public StreamingChatModel qwenStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(streamingChatModel)
                .listeners(List.of(chatModelListener))
                .build();
    }
}
