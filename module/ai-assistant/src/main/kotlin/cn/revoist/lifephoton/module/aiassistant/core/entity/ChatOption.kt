package cn.revoist.lifephoton.module.aiassistant.core.entity

/**
 * @author 6hisea
 * @date  2025/11/13 12:15
 * @description: None
 */
data class ChatOption(
    val modelName: String = "qwen-plus",
    val enableKnowledgeBase: Boolean =true,
    val enablePublicKnowledgeBase: Boolean = true,
    val enablePrivateKnowledgeBase: Boolean = true,
    val enableShareKnowledgeBase: Boolean = true,
    val enableWebSearch: Boolean = true,
    val enableTools: Boolean = true,
    val role: String = "default",
    val toolGroup: String = "all",
    val once:Boolean = false,
    //非标准参数
    val memoryId:String = "",
    val message:String = "who are you?",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatOption

        if (enableKnowledgeBase != other.enableKnowledgeBase) return false
        if (enablePublicKnowledgeBase != other.enablePublicKnowledgeBase) return false
        if (enablePrivateKnowledgeBase != other.enablePrivateKnowledgeBase) return false
        if (enableShareKnowledgeBase != other.enableShareKnowledgeBase) return false
        if (enableWebSearch != other.enableWebSearch) return false
        if (enableTools != other.enableTools) return false
        if (modelName != other.modelName) return false
        if (role != other.role) return false
        if (toolGroup != other.toolGroup) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enableKnowledgeBase.hashCode()
        result = 31 * result + enablePublicKnowledgeBase.hashCode()
        result = 31 * result + enablePrivateKnowledgeBase.hashCode()
        result = 31 * result + enableShareKnowledgeBase.hashCode()
        result = 31 * result + enableWebSearch.hashCode()
        result = 31 * result + enableTools.hashCode()
        result = 31 * result + modelName.hashCode()
        result = 31 * result + role.hashCode()
        result = 31 * result + toolGroup.hashCode()
        return result
    }
}