package cn.revoist.lifephoton.module.aiassistant.impl.rag

import dev.langchain4j.model.chat.request.DefaultChatRequestParameters

/**
 * @author 6hisea
 * @date  2025/11/12 20:53
 * @description: None
 */
class FixChatRequestParameters(val builder: Builder<*>) : DefaultChatRequestParameters(builder) {
    val extra_body = HashMap<String, Any>()
}