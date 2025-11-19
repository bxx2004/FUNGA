package cn.revoist.lifephoton.module.aiassistant.impl.chat

import dev.langchain4j.invocation.InvocationParameters
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.TokenStream
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.memory.ChatMemoryAccess

/**
 * @author 6hisea
 * @date  2025/11/11 09:29
 * @description: None
 */
interface ChatAssistant : ChatMemoryAccess {
    fun chat(@MemoryId memoryId: String, @UserMessage message: String, parameters: InvocationParameters): TokenStream
}