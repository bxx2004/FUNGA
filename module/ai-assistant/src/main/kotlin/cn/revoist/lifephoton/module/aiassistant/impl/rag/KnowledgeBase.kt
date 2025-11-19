package cn.revoist.lifephoton.module.aiassistant.impl.rag

import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import cn.revoist.lifephoton.module.aiassistant.AIAssistantAPI
import cn.revoist.lifephoton.module.aiassistant.impl.db.milvusClient
import cn.revoist.lifephoton.module.aiassistant.impl.db.useVectorDatabase
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey
import io.milvus.param.dml.DeleteParam
import java.util.concurrent.ConcurrentHashMap


/**
 * @author 6hisea
 * @date  2025/11/11 11:36
 * @description: None
 */
class KnowledgeBase private constructor(val tableName: String, minStore: Double = 0.7, minResult:Int = 1){
    private constructor(tableIndex: Int): this(tableIndex.toString())
    private val store = AIAssistant.dataManager.useVectorDatabase("knowledge_$tableName")
    private val contentRetriever = EmbeddingStoreContentRetriever.builder()
        .embeddingModel(AIAssistantAPI.embeddingModel)
        .embeddingStore(store)
        .minScore(minStore)
        .maxResults(minResult)
        .dynamicFilter{query ->
            if (tableName == "public") return@dynamicFilter metadataKey("doc_id").containsString("-")
            val uploaders = query.metadata().invocationParameters().get<List<String>>("uploaders")
            metadataKey("uploader").isIn(uploaders)
        }.build()

    fun addDocument(docs:List<TextSegment>){
        val embeddingsResponse = AIAssistantAPI.embeddingModel.embedAll(docs)
        store.addAll(embeddingsResponse.content(), docs)
    }
    fun removeDocument(docId: String){
        store.milvusClient().delete(
            DeleteParam.newBuilder()
                .withCollectionName("publicKnowledgeBase")
                .withExpr("meta[\"doc_id\"] == $docId")
                .build()
        )
    }
    fun use(): EmbeddingStoreContentRetriever{
        return contentRetriever
    }
    companion object{
        private val cache = ConcurrentHashMap<String, KnowledgeBase>()
        val public = KnowledgeBase("public")
        fun create(tableName: String, minStore: Double = 0.7, minResult:Int = 1):KnowledgeBase{
            return cache.computeIfAbsent(tableName) { KnowledgeBase(tableName,minStore,minResult) }
        }
        fun create(tableName: Int, minStore: Double = 0.7, minResult:Int = 1):KnowledgeBase{
            return cache.computeIfAbsent(tableName.toString()) { KnowledgeBase(tableName.toString(),minStore,minResult) }
        }
    }
}