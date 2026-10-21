package com.yeyuchunlan.aicodehelper.controller;

import com.yeyuchunlan.aicodehelper.ai.AiCodeHelperService;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


/**
 * AI 对话接口。
 * 返回 Flux<ServerSentEvent<String>>：模型每生成一段文字就推送一个 SSE 事件，
 * 前端用 EventSource 接收，因此回答是逐字出现的。
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiCodeHelperService aiCodeHelperService;

    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(int memoryId, String message) {
        // chatStream 是接口方法，实际执行的是 AiServices 生成的代理实现
        return aiCodeHelperService.chatStream(memoryId, message)
                // 把每个 token 包装成一个 SSE 事件推给前端
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}


