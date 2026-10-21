package com.yeyuchunlan.aicodehelper.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 直接调用 ChatModel 的示例，不经过 AiServices 代理。
 * 目前主流程走的是 AiCodeHelperService，本类保留用于对比最底层的调用方式。
 */
@Service
@Slf4j
public class AiCodeHelper {

    @Resource
    private ChatModel qwenChatModel;

    @Resource
    private ResourceLoader resourceLoader;

    private String systemMessage;

    // 与 AiCodeHelperService 共用同一份提示词，避免两处内容不一致
    @PostConstruct
    void loadSystemMessage() throws IOException {
        systemMessage = resourceLoader.getResource("classpath:system-prompt.txt")
                .getContentAsString(StandardCharsets.UTF_8);
    }

    // 最底层的调用方式：直接传一条系统消息 + 一条用户消息给模型
    public String chat(String message) {
        SystemMessage systemPrompt = SystemMessage.from(systemMessage);
        UserMessage userMessage = UserMessage.from(message);
        ChatResponse chatResponse = qwenChatModel.chat(systemPrompt, userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("AI输出：{}", aiMessage);
        return aiMessage.text();
    }

    // 多模态示例：UserMessage 里可以同时放文字和图片
    public String chatWithMessage(UserMessage userMessage) {
        ChatResponse chatResponse = qwenChatModel.chat(userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("AI输出：{}", aiMessage);
        return aiMessage.text();
    }
}
