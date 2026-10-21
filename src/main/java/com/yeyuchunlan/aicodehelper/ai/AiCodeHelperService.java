package com.yeyuchunlan.aicodehelper.ai;

import com.yeyuchunlan.aicodehelper.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 服务接口：只声明方法，不写实现类。
 * LangChain4j 的 AiServices 会在运行时为它生成代理实现，负责拼装提示词、
 * 读写对话记忆、执行 RAG 检索和工具调用。用法类似 MyBatis 的 Mapper。
 */
@InputGuardrails({SafeInputGuardrail.class})   // 每次调用前先经过输入护栏校验
public interface AiCodeHelperService {
    // 简单文本对话：系统提示词从 resources 根目录的 system-prompt.txt 读取
    @SystemMessage(fromResource="system-prompt.txt")
    String chat(String userMessage);

    // 结构化输出的返回类型：模型返回的 JSON 会按这两个字段自动转成对象
    record Report(String name, List<String> suggestionList){}

    // 结构化输出：让模型按 Report 的结构返回，而不是一段自由文本
    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatForReport(String userMessage);


    // RAG 检索：返回 Result 而不是 String，是为了同时拿到答案和命中的原文片段
    @SystemMessage(fromResource = "system-prompt.txt")
    Result<String> chatWithRag(String userMessage);

    // 流式对话：@MemoryId 标记会话 ID，框架据此为每个会话维护独立记忆；
    // 返回 Flux 表示 token 一段段推送，而不是等全部生成完再返回
    // 注意：系统提示词必须显式标注，否则流式接口不会带上人设和范围约束
    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);


}
