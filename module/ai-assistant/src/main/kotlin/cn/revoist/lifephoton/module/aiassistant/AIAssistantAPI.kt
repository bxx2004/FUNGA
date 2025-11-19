package cn.revoist.lifephoton.module.aiassistant

import cn.revoist.lifephoton.module.aiassistant.core.entity.ChatOption
import cn.revoist.lifephoton.module.aiassistant.impl.rag.KnowledgeBaseAggregator
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatRequestParameters
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import java.util.concurrent.ConcurrentHashMap

object AIAssistantAPI{
    var embeddingModel:EmbeddingModel = OllamaEmbeddingModel.builder()
        .baseUrl(AIAssistant.getConfig("embedding.url","http://localhost:1966"))
        .modelName(AIAssistant.getConfig("embedding.modelName","http://localhost:1966"))
        .build()
    var mxbaiEmbeddingModel:EmbeddingModel = OllamaEmbeddingModel.builder()
        .baseUrl(AIAssistant.getConfig("embedding.url","http://localhost:1966"))
        .modelName("mxbai-embed-large")
        .build()
    val streamingChatModel : (String?) -> StreamingChatModel = { name->
        OpenAiStreamingChatModel.builder()
            .baseUrl(AIAssistant.getConfig("llm.url","https://api.deepseek.com"))
            .modelName(name?:AIAssistant.getConfig("llm.modelName","deepseek-reasoner"))
            .temperature(AIAssistant.getConfig("llm.temperature","0.8").toDouble())
            .apiKey(AIAssistant.getConfig("llm.key","sk-xxxxxxxxxxxx"))
            .returnThinking(true)
            .defaultRequestParameters(OpenAiChatRequestParameters.EMPTY)
            .build()
    }
    val chatModel : (String?) -> ChatModel = { name->
        OpenAiChatModel.builder()
            .baseUrl(AIAssistant.getConfig("llm.url","https://api.deepseek.com"))
            .modelName(name?:AIAssistant.getConfig("llm.modelName","deepseek-reasoner"))
            .temperature(AIAssistant.getConfig("llm.temperature","0.8").toDouble())
            .apiKey(AIAssistant.getConfig("llm.key","sk-xxxxxxxxxxxx"))
            .returnThinking(true)
            .build()
    }
    var forceUpdate = false

    fun mxbaiEmbedding(text:String):List<Float>{
        return mxbaiEmbeddingModel.embed(text).content().vector().toList()
    }

    private val tools = ConcurrentHashMap<String, List<Any>>()

    fun registerTool(name:String,tool:List<Any>){
        tools[name] = tool
    }
    fun getTools(name: String):List<Any>?{
        if (name == "all") return tools.values.flatten()
        return tools[name]
    }
    fun <T>createAIAssistant(clazz:Class<T>,user_id:Long,option: ChatOption):T{
        return if (option.once){
            KnowledgeBaseAggregator.createOnceChat(user_id,clazz,option)
        }else{
            KnowledgeBaseAggregator.createStreamChat(user_id,clazz,option)
        }
    }
}