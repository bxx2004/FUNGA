package cn.revoist.lifephoton.module.funga.ai

import cn.revoist.lifephoton.module.funga.FungaPlugin
import dev.langchain4j.model.chat.Capability
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel


/**
 * @author 6hisea
 * @date  2025/4/20 15:01
 * @description: None
 */
object ModelStore {
    val DeepSeekKey = FungaPlugin.getConfig("ai.deepseek","xxxxxx")
    val GLMKey = FungaPlugin.getConfig("ai.chatglm","xxxxxx")
    val QwenKey = FungaPlugin.getConfig("ai.qwen","xxxxxx")
    var qwenMax = OpenAiChatModel.builder()
        .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
        .modelName("qwen-max")
        .temperature(0.1)
        .apiKey(QwenKey)
        .build()
    var deepSeekV3 = OpenAiChatModel.builder()
        .baseUrl("https://api.deepseek.com")
        .modelName("deepseek-chat")
        .temperature(0.0)
        .apiKey(DeepSeekKey)
        .build()
    var deepSeekV3Chat = OpenAiStreamingChatModel.builder()
        .baseUrl("https://api.deepseek.com")
        .modelName("deepseek-chat")
        .temperature(1.0)
        .apiKey(DeepSeekKey)
        .build()
}