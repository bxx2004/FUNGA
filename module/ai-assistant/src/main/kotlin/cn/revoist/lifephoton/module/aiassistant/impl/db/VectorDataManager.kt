package cn.revoist.lifephoton.module.aiassistant.impl.db

import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import cn.revoist.lifephoton.plugin.data.DataManager
import cn.revoist.lifephoton.tools.getProperty
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore
import io.milvus.client.MilvusServiceClient
import io.milvus.common.clientenum.ConsistencyLevelEnum
import io.milvus.param.IndexType
import io.milvus.param.MetricType

/**
 * @author 6hisea
 * @date  2025/11/11 11:51
 * @description: None
 */
fun DataManager.useVectorDatabase(collectionName: String, dbName: String = plugin.name) : MilvusEmbeddingStore {
    val clean = dbName.replace("-","")
    val host = AIAssistant.getConfig("milvus.host","localhost")
    val port = AIAssistant.getConfig("milvus.port","19530")
    val username = AIAssistant.getConfig("milvus.username","root")
    val password = AIAssistant.getConfig("milvus.password","123456")
    return MilvusEmbeddingStore.builder()
        .host(host)
        .databaseName(clean)// Host for Milvus instance
        .port(port.toInt())                               // Port for Milvus instance
        .collectionName(collectionName)      // Name of the collection
        .dimension(1024)                            // Dimension of vectors
        .indexType(IndexType.DISKANN)                 // Index type
        .metricType(MetricType.COSINE)             // Metric type
        .username(username)                      // Username for Milvus
        .password(password)                      // Password for Milvus
        .consistencyLevel(ConsistencyLevelEnum.EVENTUALLY)  // Consistency level
        .autoFlushOnInsert(true)                   // Auto flush after insert
        .idFieldName("id")                         // ID field name
        .textFieldName("text")                     // Text field name
        .metadataFieldName("metadata")             // Metadata field name
        .vectorFieldName("vector")                 // Vector field name
        .build();
}
fun MilvusEmbeddingStore.milvusClient(): MilvusServiceClient {
    return getProperty<MilvusServiceClient>("milvusClient")!!
}