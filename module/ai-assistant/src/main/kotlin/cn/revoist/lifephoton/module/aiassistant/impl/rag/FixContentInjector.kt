package cn.revoist.lifephoton.module.aiassistant.impl.rag

import cn.revoist.lifephoton.plugin.data.sqltype.gson
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.TextContent
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.input.Prompt
import dev.langchain4j.model.input.PromptTemplate
import dev.langchain4j.rag.content.Content
import dev.langchain4j.rag.content.injector.ContentInjector
import java.util.stream.Collectors

/**
 * Default implementation of [FixContentInjector] intended to be suitable for the majority of use cases.
 * <br></br>
 * <br></br>
 * It's important to note that while efforts will be made to avoid breaking changes,
 * the default behavior of this class may be updated in the future if it's found
 * that the current behavior does not adequately serve the majority of use cases.
 * Such changes would be made to benefit both current and future users.
 * <br></br>
 * <br></br>
 * This implementation appends all given [Content]s to the end of the given [UserMessage]
 * in their order of iteration.
 * Refer to [.DEFAULT_PROMPT_TEMPLATE] and implementation for more details.
 * <br></br>
 * <br></br>
 * Configurable parameters (optional):
 * <br></br>
 * - [.promptTemplate]: The prompt template that defines how the original `userMessage`
 * and `contents` are combined into the resulting [UserMessage].
 * The text of the template should contain the `{{userMessage}}` and `{{contents}}` variables.
 * <br></br>
 * - [.metadataKeysToInclude]: A list of [Metadata] keys that should be included
 * with each [Content.textSegment].
 */
class FixContentInjector(
    metadataKeysToInclude: List<String>
) : ContentInjector {
    private val promptTemplate: PromptTemplate = DEFAULT_PROMPT_TEMPLATE
    private val metadataKeysToInclude: List<String>



    init {
        this.metadataKeysToInclude = metadataKeysToInclude
    }

    override fun inject(contents: MutableList<Content>, chatMessage: ChatMessage): ChatMessage {
        if (contents.isEmpty()) {
            return chatMessage
        }

        val prompt = createPrompt(chatMessage, contents)
        return if (chatMessage is UserMessage) {
            chatMessage.toBuilder()
                .contents(listOf(TextContent.from(prompt.text())))
                .build()
        } else {
            prompt.toUserMessage()
        }
    }

    fun createPrompt(chatMessage: ChatMessage, contents: MutableList<Content>): Prompt {
        val variables: MutableMap<String, Any> = HashMap()
        variables.put("contents", gson.toJson(format((chatMessage as UserMessage).singleText(),contents)))
        return promptTemplate.apply(variables)
    }
    //元信息附带
    fun format(metadata: Metadata): Map<String, Any?> {
        val res = hashMapOf<String, Any?>()
        for (metadataKey in metadataKeysToInclude) {
            val metadataValue = metadata.toMap()[metadataKey]
            res[metadataKey] = metadataValue
        }
        return res
    }


    data class UserMessageTransformer(
        val message: String,
        val documents:List<DocumentTransformer>
    )

    data class DocumentTransformer(
        val metadata:Map<String, Any?>,
        val content:String
    )
    //列表内容
    fun format(um: String, contents: List<Content>): UserMessageTransformer {
        return UserMessageTransformer(um,contents.map { format(it) })
    }
    //内容
    fun format(content: Content): DocumentTransformer {
        val segment = content.textSegment()
        val content = segment.text()
        val metadata = format(segment.metadata())
        return DocumentTransformer(metadata,content)
    }

    class FixContentInjectorBuilder internal constructor() {
        private var metadataKeysToInclude: List<String>? = null

        fun metadataKeysToInclude(metadataKeysToInclude: List<String>): FixContentInjectorBuilder {
            this.metadataKeysToInclude = metadataKeysToInclude
            return this
        }

        fun build(): FixContentInjector {
            return FixContentInjector(metadataKeysToInclude = this.metadataKeysToInclude!!)
        }
    }

    companion object {
        val DEFAULT_PROMPT_TEMPLATE: PromptTemplate = PromptTemplate.from("{{contents}}")

        fun builder(): FixContentInjectorBuilder {
            return FixContentInjectorBuilder()
        }
    }
}