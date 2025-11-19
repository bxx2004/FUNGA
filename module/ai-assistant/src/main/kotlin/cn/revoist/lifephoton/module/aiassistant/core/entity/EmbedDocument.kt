package cn.revoist.lifephoton.module.aiassistant.core.entity

import cn.revoist.lifephoton.module.aiassistant.impl.rag.KnowledgeBase

/**
 * @author 6hisea
 * @date  2025/11/12 16:07
 * @description: None
 */
data class EmbedDocument(val citation:String,val knowledgeBase: KnowledgeBase?,val docId:String,val uploader: Long = -1L, val file : ByteArray? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmbedDocument

        if (uploader != other.uploader) return false
        if (!file.contentEquals(other.file)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uploader.hashCode()
        result = 31 * result + (file?.contentHashCode() ?: 0)
        return result
    }

}