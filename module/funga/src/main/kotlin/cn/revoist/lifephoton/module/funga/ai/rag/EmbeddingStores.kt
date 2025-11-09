package cn.revoist.lifephoton.module.funga.ai.rag

import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.ai.embedding.SentencesEmbedding
import cn.revoist.lifephoton.module.funga.data.table.LiteratureTable
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore
import io.milvus.common.clientenum.ConsistencyLevelEnum
import io.milvus.param.IndexType
import io.milvus.param.MetricType
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import java.io.File
import kotlin.math.floor


/**
 * @author 6hisea
 * @date  2025/7/26 19:42
 * @description: None
 */
object EmbeddingStores {
    var parser = ApachePdfBoxDocumentParser(false)
    var current = 0
    fun getAllDb():List<MilvusEmbeddingStore>{
        val res = mutableListOf<MilvusEmbeddingStore>()
        var cache = 0
        while (true) {
            if (cache <= current) {
                res.add(getLiteratureBase(cache))
                cache++
            }else{
                break
            }
        }
        return res
    }
    fun getLiteratureBase(index:Int): MilvusEmbeddingStore{

        return MilvusEmbeddingStore.builder()
            .host(FungaPlugin.getConfig("milvus.host","http://localhost"))
            .port(FungaPlugin.getConfig("milvus.port","19530").toInt())
            .collectionName("literature_${index}")      // 集合名称
            .dimension(1024)                            // 向量维度
            .indexType(IndexType.FLAT)                 // 索引类型
            .metricType(MetricType.COSINE)             // 度量类型
            .consistencyLevel(ConsistencyLevelEnum.EVENTUALLY)  // 一致性级别
            .autoFlushOnInsert(true)                   // 插入后自动刷新
            .idFieldName("id")                         // ID 字段名称
            .textFieldName("text")                     // 文本字段名称
            .metadataFieldName("metadata")             // 元数据字段名称
            .vectorFieldName("vector")                 // 向量字段名称
            .build()
    }

    fun upload(file: File,user: Long,citation: String,title:String){
        val size = FungaPlugin.dataManager.useDatabase()
            .from(LiteratureTable)
            .select(LiteratureTable.id)
            .map { it }.size
        val page = floor(size / 100000.toDouble()).toInt()
        current = page
        val document = FileSystemDocumentLoader.loadDocument(file.absolutePath,parser)
        document.metadata().put("citation", citation)
        document.metadata().put("user", user)
        document.metadata().put("title", title)
        val es = getLiteratureBase(page)
        EmbeddingStoreIngestor.builder()
            .embeddingModel(SentencesEmbedding.model.value)
            .embeddingStore(es)
            .build().ingest(document)
    }
}