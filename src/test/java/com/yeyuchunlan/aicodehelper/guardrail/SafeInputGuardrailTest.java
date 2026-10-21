package com.yeyuchunlan.aicodehelper.guardrail;

import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeInputGuardrailTest {

    private final SafeInputGuardrail guardrail = new SafeInputGuardrail();

    @Test
    void allowsNormalInput() {
        assertTrue(guardrail.validate(UserMessage.from("怎么学习 Java？")).isSuccess());
    }

    @Test
    void rejectsSensitiveWord() {
        assertFalse(guardrail.validate(UserMessage.from("kill the process")).isSuccess());
    }

    @Test
    void isCaseInsensitive() {
        assertFalse(guardrail.validate(UserMessage.from("EVIL")).isSuccess());
    }
}
