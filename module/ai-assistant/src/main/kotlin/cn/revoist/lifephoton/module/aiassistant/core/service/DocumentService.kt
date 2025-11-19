package cn.revoist.lifephoton.module.aiassistant.core.service

import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import cn.revoist.lifephoton.module.aiassistant.impl.db.computeTableIndex
import cn.revoist.lifephoton.module.aiassistant.core.entity.DocumentMapper
import cn.revoist.lifephoton.module.aiassistant.core.entity.EmbedDocument
import cn.revoist.lifephoton.module.aiassistant.impl.rag.DocumentEmbeder
import cn.revoist.lifephoton.module.aiassistant.impl.rag.DocumentTable
import cn.revoist.lifephoton.module.aiassistant.impl.rag.KnowledgeBase
import cn.revoist.lifephoton.module.authentication.data.table.hasFriend
import cn.revoist.lifephoton.module.authentication.data.table.whoShareMe
import cn.revoist.lifephoton.module.authentication.lastUserId
import cn.revoist.lifephoton.module.authentication.userSize
import cn.revoist.lifephoton.plugin.computeDynamicInformation
import cn.revoist.lifephoton.plugin.data.count
import cn.revoist.lifephoton.plugin.data.json.JSONObject
import cn.revoist.lifephoton.plugin.data.json.jsonObject
import cn.revoist.lifephoton.plugin.data.pool.DynamicPageInformation
import cn.revoist.lifephoton.plugin.route.PagingPayloadResponse
import net.lingala.zip4j.ZipFile
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.forEach
import org.jetbrains.kotlinx.dataframe.io.readExcel
import org.ktorm.dsl.and
import org.ktorm.dsl.asc
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.gte
import org.ktorm.dsl.inList
import org.ktorm.dsl.insert
import org.ktorm.dsl.like
import org.ktorm.dsl.limit
import org.ktorm.dsl.map
import org.ktorm.dsl.or
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import org.ktorm.schema.Column
import org.ktorm.schema.ColumnDeclaring
import java.io.File
import java.io.InputStream
import java.util.UUID
import kotlin.collections.map
import kotlin.math.ceil

/**
 * @author 6hisea
 * @date  2025/11/11 13:05
 * @description: None
 */
object DocumentService {


    fun statics(userId: Long): JSONObject{
        val table = DocumentTable.create(userId.computeTableIndex())
        val myCount = AIAssistant.dataManager.useDatabase().count(table){
            table.uploader eq userId
        }
        val friends = userId.whoShareMe()

        val tables = friends.map { DocumentTable.create(it.computeTableIndex()) }
        val shareCount = if (tables.isNotEmpty()) tables.sumOf {
            AIAssistant.dataManager.useDatabase().count(it) {
                it.uploader inList friends
            }
        } else 0
        val publicCount = AIAssistant.dataManager.useDatabase().count(DocumentTable.public){
            DocumentTable.public.id gte 1
        }

        val tables2 = (0 until lastUserId().computeTableIndex()).map {
            DocumentTable.create(it)
        }

        val c2 = if (tables2.isNotEmpty()) tables2.sumOf {
            AIAssistant.dataManager.useDatabase().count(it) {
                it.id gte 1
            }
        } else 0
        return jsonObject {
            put("myCount", myCount)
            put("shareCount", shareCount)
            put("publicCount", publicCount)
            put("allCount", c2 + publicCount)
        }
    }

    fun search(userId: Long,keyword:String,size: Int): PagingPayloadResponse<*> {
        return AIAssistant.dataManager.useDynamicPagination(size) { requestPage, size ->
            val tables = arrayListOf<DocumentTable>()
            tables.add(DocumentTable.create(userId.computeTableIndex()))
            val friends = userId.whoShareMe()
            tables.addAll(friends.map { DocumentTable.create(it.computeTableIndex()) })
            tables.add(DocumentTable.public)
            val count = tables.sumOf {
                AIAssistant.dataManager.useDatabase().count(it) {
                    ((it.uploader inList friends) or (it.uploader eq userId)) and ((it.citation like "%$keyword%") or (it.doc_id eq keyword))
                }
            }
            val pages = ceil(count / size.toDouble()).toInt()

            return@useDynamicPagination computeDynamicInformation(requestPage, size, pages, AIAssistant,tables,listOf(
                "file"
            )){
                ((this["uploader"] as Column<Long> inList friends) or (this["uploader"] as Column<Long> eq userId)) and ((this["citation"] like "%$keyword%") or (this["doc_id"] as Column<String> eq keyword))
            }
        }
    }

    fun getDocuments(userId: Long,size:Int): PagingPayloadResponse<*> {
        val table = DocumentTable.create(userId.computeTableIndex())
        return AIAssistant.dataManager.useDynamicPagination (size){ requestPage, size ->
            val count = AIAssistant.dataManager.useDatabase().count(table){
                table.uploader eq userId
            }
            val pages = ceil(count / size.toDouble()).toInt()
            return@useDynamicPagination computeDynamicInformation(requestPage, size, pages, AIAssistant,table,listOf("file")){
                table.uploader eq userId
            }
        }
    }

    fun getShareDocuments(userId: Long,size:Int): PagingPayloadResponse<*> {
        val friends = userId.whoShareMe()

        return AIAssistant.dataManager.useDynamicPagination(size) { requestPage, size ->
            val tables = friends.map { DocumentTable.create(it.computeTableIndex()) }
            if (tables.isEmpty()) {
                return@useDynamicPagination DynamicPageInformation(listOf(),1)
            }
            val count = tables.sumOf {
                AIAssistant.dataManager.useDatabase().count(it) {
                    it.uploader inList friends
                }
            }
            val pages = ceil(count / size.toDouble()).toInt()

            return@useDynamicPagination computeDynamicInformation(requestPage, size, pages, AIAssistant,tables,listOf(
                "file"
            )){
                (this["uploader"] as ColumnDeclaring<Long>) inList friends
            }
        }
    }
    fun getPublicDocuments(size:Int): PagingPayloadResponse<*> {
        return AIAssistant.dataManager.useDynamicPagination(size) { requestPage, size ->
            val count = AIAssistant.dataManager.useDatabase().count(DocumentTable.public){
                DocumentTable.public.id gte 1
            }
            val pages = ceil(count / size.toDouble()).toInt()
            return@useDynamicPagination computeDynamicInformation(requestPage, size, pages, AIAssistant,DocumentTable.public,listOf("file")){
                DocumentTable.public.id gte 1
            }
        }
    }
    fun uploadDocument(userId: Long,file: File,citation: String,source:String,isPublic: Boolean){
        uploadDocument(userId,file.inputStream(),citation,source,isPublic)
    }
    fun uploadDocument(userId: Long,stream: InputStream,citation: String,source:String,isPublic: Boolean){
        val table = if (isPublic) DocumentTable.public else DocumentTable.create(userId.computeTableIndex())
        AIAssistant.dataManager.useDatabase()
            .insert(table){
                set(it.citation,citation)
                set(it.source,source)
                set(it.review, !isPublic)
                set(it.file,stream.readBytes())
                set(it.date,System.currentTimeMillis())
                set(it.embed,false)
                set(it.doc_id, UUID.randomUUID().toString())
                set(it.uploader,userId)
            }
        if (isPublic){
            DocumentEmbeder.startPublicDocumentTask()
        }else{
            DocumentEmbeder.startPrivateDocumentTask()
        }
        stream.close()
    }

    fun deleteDocument(userId: Long,docId:String){
        val tables = arrayListOf<DocumentTable>()
        tables.add(DocumentTable.create(userId.computeTableIndex()))
        tables.add(DocumentTable.public)
        tables.forEach {
            AIAssistant.dataManager.useDatabase()
                .delete(
                    it
                ){
                    (it.doc_id eq docId) and (it.uploader eq userId)
                }
        }
    }

    //阅读索引文件并上传所有文件
    fun uploadDocuments(userId: Long,file: File,isPublic: Boolean){
        val zipFile = ZipFile(file)
        val xlsx = zipFile.getFileHeader("index.xlsx")
        //文件名称，引用，来源
        DataFrame.readExcel(zipFile.getInputStream(xlsx), firstRowIsHeader = true, columns = "A:C")
            .forEach {
                val filePath = it["file_name"].toString()
                val citation = it["citation"].toString()
                val source = it["source"].toString()
                val file = zipFile.getFileHeader(filePath)
                zipFile.getInputStream(file).use { stream ->
                    uploadDocument(userId,stream,citation,source,isPublic)
                }
            }
        zipFile.close()
    }
    //阅读原文档
    fun getFile(userId:Long,uploader:Long,docId:String,isPublic:Boolean):ByteArray?{
        val table = if (isPublic) DocumentTable.public else DocumentTable.create(uploader.computeTableIndex())
        return AIAssistant.dataManager.useDatabase()
            .from(table)
            .select(table.file)
            .where {
                (table.uploader eq uploader) and (table.doc_id eq docId)
            }
            .limit(1)
            .map {
                it.getBytes(1)
            }.firstOrNull()

    }

    fun markEmbed(docId:String,userId: Long,isPublic:Boolean){
        val table = if (isPublic){
            DocumentTable.public
        }else{
            DocumentTable.create(userId.computeTableIndex())
        }
        AIAssistant.dataManager.useDatabase()
            .update(table){
                set(it.embed,true)
                where {
                    (it.doc_id eq docId) and (it.uploader eq userId)
                }
            }
    }

    fun nextNotEmbedDocument(isPublic:Boolean): EmbedDocument{
        val tables = if (!isPublic){
            (0 until lastUserId().computeTableIndex()).map {
                DocumentTable.create(it)
            }
        }else{
            listOf(DocumentTable.public)
        }
        for (table in tables) {
            val ed = AIAssistant.dataManager.useDatabase()
                .from(table)
                .select(table.file,table.uploader,table.doc_id,table.citation)
                .where {
                    (table.embed eq false) and (table.review eq true)
                }
                .limit(1)
                .orderBy(table.date.asc())
                .map {
                    EmbedDocument(it.get(table.citation).toString(),KnowledgeBase.create(table.tableName.split("_")[1]),it.get(table.doc_id)!!,it.get(table.uploader)!!,it.get(table.file))
                }.firstOrNull()
            if (ed?.file != null){
                return ed
            }
        }
        return EmbedDocument("",null,"",-1,null)
    }

}