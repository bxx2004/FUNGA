package cn.revoist.lifephoton.module.funga.ai.chat

import cn.revoist.lifephoton.module.funga.ai.ModelStore
import cn.revoist.lifephoton.module.funga.ai.chat.tools.AIFungaTools
import cn.revoist.lifephoton.module.funga.ai.chat.tools.AINetworkTools
import cn.revoist.lifephoton.module.funga.ai.embedding.SentencesEmbedding
import cn.revoist.lifephoton.module.funga.ai.rag.EmbeddingStores
import cn.revoist.lifephoton.module.funga.ai.rag.MultiEmbeddingStoreContentRetriever
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.ChatMemoryAccess
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.TokenStream
import dev.langchain4j.service.UserMessage


/**
 * @author 6hisea
 * @date  2025/5/26 19:48
 * @description: None
 */
interface TalkBot : ChatMemoryAccess {
    @SystemMessage("你是一个FUNGA平台的生物信息学小助手，你将能准确的推断生物信息学的相关知识并进行纠错。")
    fun chat(@MemoryId memoryId: Int, @UserMessage message: String): TokenStream
    companion object{
        val INSTANCE = AiServices.builder(TalkBot::class.java)
            .streamingChatLanguageModel(ModelStore.deepSeekV3Chat)
            .chatMemoryProvider { id -> MessageWindowChatMemory.withMaxMessages(10) }
            .tools(AIFungaTools, AINetworkTools)
            .contentRetriever(MultiEmbeddingStoreContentRetriever(SentencesEmbedding.model.value))
            .build()
    }
}