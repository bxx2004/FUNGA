package cn.revoist.lifephoton.module.funga.data.database.vector

import cn.revoist.lifephoton.module.aiassistant.AIAssistantAPI
import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.core.info.table.DBInfoTable
import cn.revoist.lifephoton.module.funga.data.table.GeneOntologyTable
import cn.revoist.lifephoton.module.funga.data.table.GenePhenotypeTable
import cn.revoist.lifephoton.module.funga.data.table.GeneTable
import cn.revoist.lifephoton.module.funga.data.table.PhenotypeOntologyQualifierTable
import cn.revoist.lifephoton.module.funga.data.table.PhenotypeOntologyTable
import cn.revoist.lifephoton.plugin.data.entity.Index
import cn.revoist.lifephoton.plugin.data.json.JSONObject
import cn.revoist.lifephoton.plugin.data.json.jsonObject
import cn.revoist.lifephoton.plugin.data.maps
import cn.revoist.lifephoton.plugin.data.processor.MergeData
import cn.revoist.lifephoton.plugin.data.processor.join
import cn.revoist.lifephoton.plugin.data.sqltype.ObjectType
import cn.revoist.lifephoton.plugin.data.sqltype.gson
import cn.revoist.lifephoton.plugin.properties
import com.google.common.collect.Lists
import com.google.gson.JsonObject
import io.milvus.v2.client.ConnectConfig
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.common.DataType
import io.milvus.v2.common.IndexParam
import io.milvus.v2.service.collection.request.AddFieldReq
import io.milvus.v2.service.collection.request.CreateCollectionReq
import io.milvus.v2.service.collection.request.HasCollectionReq
import io.milvus.v2.service.index.request.CreateIndexReq
import io.milvus.v2.service.vector.request.AnnSearchReq
import io.milvus.v2.service.vector.request.HybridSearchReq
import io.milvus.v2.service.vector.request.InsertReq
import io.milvus.v2.service.vector.request.QueryIteratorReq
import io.milvus.v2.service.vector.request.data.BaseVector
import io.milvus.v2.service.vector.request.data.FloatVec
import io.milvus.v2.service.vector.request.ranker.WeightedRanker
import org.ktorm.schema.Table
import org.ktorm.schema.VarcharSqlType
import java.io.File
import java.util.ArrayList
import java.util.Collections
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.kotlinProperty

/**
 * @author 6hisea
 * @date  2025/4/16 12:42
 * @description: None
 */
object MilvusDatabase {
    private val config = lazy { ConnectConfig.builder()
        .uri(FungaPlugin.getConfig("milvus.url","http://localhost:19530"))
        .build() }
    lateinit var client: MilvusClientV2

    fun init(){
        client = MilvusClientV2(config.value)
        val onlineDBs = FungaPlugin.dataManager.useDatabase()
            .maps(DBInfoTable, DBInfoTable.id)
            .map {
                it["name"] as String
            }
        onlineDBs.forEach {
            GenePhenotypeTable.createMilvusDatabaseIfNotExists(it,arrayListOf("phenotype"))
            PhenotypeOntologyTable.createMilvusDatabaseIfNotExists(it,arrayListOf("description"))
            GeneTable.createMilvusDatabaseIfNotExists(it,arrayListOf("description"))
            PhenotypeOntologyQualifierTable.createMilvusDatabaseIfNotExists(it,arrayListOf("description"))

        }
        GeneOntologyTable.createMilvusDatabaseIfNotExists("funga",arrayListOf("term"))
    }

    private fun createCollectionIfNotExists(name: String, schema: CreateCollectionReq.CollectionSchema, vector_name: List<String>){
        val hasGOCollection = client.hasCollection(
            HasCollectionReq.builder().collectionName(name).build()
        )
        if (!hasGOCollection){
            client.createCollection(
                CreateCollectionReq.builder()
                    .collectionName(name)
                    .collectionSchema(schema)
                    .build()
            )
            vector_name.forEach {
                client.createIndex(
                    CreateIndexReq.builder().collectionName(name).indexParams(
                        listOf(
                            IndexParam.builder().metricType(IndexParam.MetricType.COSINE)
                                .indexType(IndexParam.IndexType.AUTOINDEX).fieldName(it + "_vector").build()
                        )
                    ).build()
                )
            }
        }
    }
    fun search(dbName:String, collectionName:String, sentences: Map<String, String>, topK:Int = 100,minScore: Double= 0.8): List<Map<String, Any>>{

        var map : ArrayList<Map<String, Any>> = ArrayList()

        val reqs = ArrayList<AnnSearchReq>()
        sentences.forEach {col,con->
            reqs.add(
                AnnSearchReq.builder()
                .vectorFieldName(col + "_vector")
                .vectors(Collections.singletonList(FloatVec(AIAssistantAPI.mxbaiEmbedding(con))) as List<BaseVector>?)
                .topK(topK)
                .build())
        }

        client.hybridSearch(
            HybridSearchReq.builder()
                .collectionName(dbName+ "_" +collectionName)
                .searchRequests(reqs)
                .ranker(WeightedRanker(arrayListOf(1.0F)))
                .topK(topK)
                .outFields(Collections.singletonList("*"))
                .build()
        ).searchResults.forEach {
            it.forEach { re->
                re.entity["score"] = re.score
                val iterator = re.entity.keys.iterator()
                while (iterator.hasNext()) {
                    if (iterator.next().contains("_vector")) {
                        iterator.remove()
                    }
                }
                if (re.score >= minScore) {
                    map.add(re.entity)
                }

            }
        }
        return map
    }

    fun Table<*>.insert(dbName: String, entities: List<Any>){
        val result = arrayListOf<JsonObject>()
        entities.forEach {entity->
            val obj = JsonObject()
            entity.properties().forEach {
                if (it.name != "id"){
                    if (it.get(entity) is List<*>){
                        obj.add(it.name, gson.toJsonTree(it.get(entity)))
                    }else if (it.get(entity) is JSONObject){
                        obj.add(it.name, gson.toJsonTree(it.get(entity)))
                    }else{
                        if (it.get(entity) == null){
                            obj.addProperty(it.name,"-")
                        }else{
                            obj.addProperty(it.name,it.get(entity).toString())
                        }
                    }
                    if(it.kotlinProperty?.hasAnnotation<Index>() == true){
                            obj.add(it.name+"_vector", gson.toJsonTree(AIAssistantAPI.mxbaiEmbedding(it.get(entity).toString())))
                    }
                }
            }
            result.add(obj)
        }
        client.insert(
            InsertReq.builder().collectionName(dbName+ "_" +tableName).data(result).build()
        )
    }

    fun Table<*>.insert(dbName: String, entity: Any){
        val obj = JsonObject()
        entity.properties().forEach {
            if (it.name != "id"){
                if (it.get(entity) is List<*>){
                    obj.add(it.name, gson.toJsonTree(it.get(entity)))
                }else if (it.get(entity) is JSONObject){
                    obj.add(it.name, gson.toJsonTree(it.get(entity)))
                }else{
                    if (it.get(entity) == null){
                        obj.addProperty(it.name,"-")
                    }else{
                        obj.addProperty(it.name,it.get(entity).toString())
                    }
                }
                if(it.kotlinProperty?.hasAnnotation<Index>() == true){
                    obj.add(it.name+"_vector", gson.toJsonTree(AIAssistantAPI.mxbaiEmbedding((it.get(entity).toString()))))
                }
            }
        }
        client.insert(
            InsertReq.builder().collectionName(dbName+ "_" +tableName).data(Collections.singletonList(obj)).build()
        )
    }

    fun Table<*>.createMilvusDatabaseIfNotExists(dbName: String, vector_name: List<String>){
        var schema = client.createSchema()
        vector_name.forEach {
            schema.addField(AddFieldReq.builder().fieldName(it+"_vector").dataType(DataType.FloatVector).dimension(1024).build())
        }
        this.columns.forEach {
            if (it.name == "id"){
                schema.addField(AddFieldReq.builder().fieldName(it.name).dataType(DataType.VarChar).isPrimaryKey(true).autoID(true).build())
            }else{
                when(it.sqlType){
                    is VarcharSqlType ->{
                        schema.addField(AddFieldReq.builder().fieldName(it.name).dataType(DataType.VarChar).build())
                    }
                    is ObjectType -> {
                        schema.addField(AddFieldReq.builder().fieldName(it.name).dataType(DataType.JSON).build())
                    }
                }
            }
        }
        createCollectionIfNotExists(dbName+"_"+tableName,schema,vector_name)
    }
    fun Table<*>.search(dbName:String, sentences: Map<String, String>, topK:Int = 100,minScore: Double = 0.8): List<Map<String, Any>> {
        return search(dbName,tableName,sentences,topK,minScore)
    }
    fun Table<*>.search(dbName:List<String>, sentences: Map<String, String>, topK:Int = 100,minScore: Double = 0.8): List<MergeData<List<Map<String, Any>>>> {
        return join(dbName) {
            search(it, tableName, sentences, topK,minScore)
        }
    }
    fun Table<*>.saveVectorData(dbName: String, dir: String){
        val data = arrayListOf<JSONObject>()
        val queryIterator = client.queryIterator(
            QueryIteratorReq.builder()
        .collectionName(dbName+"_"+tableName)
        .outputFields(Lists.newArrayList("*"))
        .batchSize(50L)
        .build())
        while (true) {
            val res = queryIterator.next()
            if (res.isEmpty()) {
                queryIterator.close();
                break;
            }
            for (record in res) {
                data.add(
                    jsonObject {
                        record.fieldValues.forEach { t, u ->
                            put(t, u)
                        }
                    }
                )
            }
        }
        val file = File(dir + "${dbName}_$tableName.json")
        if (file.exists()) {
            file.createNewFile()
        }
        file.writeText(gson.toJson(data))
    }
}