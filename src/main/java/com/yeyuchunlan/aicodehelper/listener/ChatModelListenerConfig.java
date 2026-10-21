package com.yeyuchunlan.aicodehelper.listener;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 模型调用监听器：记录每次请求、响应和异常，便于排查问题。
 * 由 QwenChatModelConfig 注册到自定义的 ChatModel 上。
 */
@Configuration
@Slf4j
public class ChatModelListenerConfig {

    @Bean
    ChatModelListener chatModelListener() {
        return new ChatModelListener() {
            // 请求发出前触发：打印发给模型的完整请求
            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                log.debug("onRequest(): {}", requestContext.chatRequest());
            }

            // 响应返回后触发：打印模型返回的完整内容
            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                log.debug("onResponse(): {}", responseContext.chatResponse());
            }

            // 调用出错时触发：打印异常信息
            @Override
            public void onError(ChatModelErrorContext errorContext) {
                log.warn("onError(): {}", errorContext.error().getMessage());
            }
        };
    }
}

