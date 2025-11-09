package cn.revoist.lifephoton.module.funga.ai.assistant

import cn.revoist.lifephoton.module.funga.ai.ModelStore
import cn.revoist.lifephoton.module.funga.ai.embedding.SentencesEmbedding
import cn.revoist.lifephoton.module.funga.ai.rag.MultiEmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.V

/**
 * @author 6hisea
 * @date  2025/4/20 15:01
 * @description: None
 */
interface PaperAssistant {
    @SystemMessage("你是一个基因挖掘类论文生成的小助手，你的任务是生成基因表型关系论文的讨论部分")
    @UserMessage("""
        1.按照以下规则生成
         - 使用英文生成
         - 具有逻辑性和推理性
         - 内容丰富，字数不要太少
         - 其中func是功能基因，predict是预测基因，outer是不在基因列表内的基因，提一下就可以了
         - 在文章结尾处，加一句“Disclaimer：The generated content is for reference only and should not be used for actual use.”
        2.输入数据如下：
         - {{data}}
        3.用户提示词如下:
         - {{prompt}}
    """)
    fun generateDiscretion(@V("data") data:String,@V("prompt")prompt: String):String









    companion object{
        val INSTANCE = AiServices.builder(PaperAssistant::class.java)
            .chatLanguageModel(ModelStore.deepSeekV3)
            .contentRetriever(MultiEmbeddingStoreContentRetriever(SentencesEmbedding.model.value))
            .build()
    }
}