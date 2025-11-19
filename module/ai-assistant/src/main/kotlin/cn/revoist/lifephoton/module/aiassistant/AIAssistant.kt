package cn.revoist.lifephoton.module.aiassistant

import cn.revoist.lifephoton.module.aiassistant.impl.rag.DocumentEmbeder
import cn.revoist.lifephoton.module.aiassistant.impl.rag.KnowledgeBaseAggregator
import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.anno.AutoUse

/**
 * @author 6hisea
 * @date  2025/11/11 09:25
 * @description: None
 */
@AutoUse
object AIAssistant : Plugin() {
    override val name: String
        get() = "ai-assistant"
    override val author: String = "bxx2004"
    override val version: String = "beta-1"

    override fun load() {
        DocumentEmbeder.startPrivateDocumentTask()
        DocumentEmbeder.startPublicDocumentTask()
        KnowledgeBaseAggregator.startListener()
    }


}