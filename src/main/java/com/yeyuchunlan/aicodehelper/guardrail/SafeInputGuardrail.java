package com.yeyuchunlan.aicodehelper.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Set;

/**
 * 安全检测输入护轨
 */
/**
 * 输入护栏：在请求发给模型之前检查用户输入。
 * 返回 success() 放行；返回 fatal(...) 拒绝本次调用并返回错误信息。
 * 这里的敏感词表只用于演示护栏机制，不是完整的内容安全方案。
 */
public class SafeInputGuardrail implements InputGuardrail {

    // 命中任意一个词就拒绝本次请求
    private static final Set<String> sensitiveWords = Set.of("kill", "evil");

    /**
     * 检测用户输入是否安全
     */
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        // 获取用户输入并转换为小写以确保大小写不敏感
        String inputText = userMessage.singleText().toLowerCase();
        // 使用正则表达式分割输入文本为单词
        String[] words = inputText.split("\\W+");
        // 遍历所有单词，检查是否存在敏感词
        for (String word : words) {
            if (sensitiveWords.contains(word)) {
                return fatal("Sensitive word detected: " + word);
            }
        }
        return success();
    }
}

