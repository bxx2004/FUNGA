package cn.revoist.lifephoton.module.aiassistant.impl.chat

import dev.langchain4j.invocation.InvocationParameters
import dev.langchain4j.service.TokenStream
import dev.langchain4j.service.UserMessage

/**
 * @author 6hisea
 * @date  2025/11/11 09:29
 * @description: None
 */
interface OnceAssistant {
    fun chat(@UserMessage message: String, parameters: InvocationParameters): String
}