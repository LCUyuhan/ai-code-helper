package com.yeyuchunlan.aicodehelper.ai;

import com.yeyuchunlan.aicodehelper.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import reactor.core.publisher.Flux;

/**
 * AI 服务接口：只声明方法，不写实现类。
 * LangChain4j 的 AiServices 会在运行时为它生成代理实现，负责拼装提示词、
 * 读写对话记忆、执行 RAG 检索和工具调用。用法类似 MyBatis 的 Mapper。
 */
@InputGuardrails({SafeInputGuardrail.class})   // 每次调用前先经过输入护栏校验
public interface AiCodeHelperService {

    // 流式对话：@MemoryId 标记会话 ID，框架据此为每个会话维护独立记忆；
    // 返回 Flux 表示 token 一段段推送，而不是等全部生成完再返回
    // 系统提示词必须显式标注，否则流式接口不会带上人设和范围约束
    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}
