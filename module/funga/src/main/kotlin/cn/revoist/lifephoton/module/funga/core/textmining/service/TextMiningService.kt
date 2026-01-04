package cn.revoist.lifephoton.module.funga.core.textmining.service

import cn.revoist.lifephoton.module.aiassistant.impl.rag.DocumentEmbeder
import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.core.textmining.assistant.TextMiningAssistant
import cn.revoist.lifephoton.module.funga.core.textmining.table.TextMiningTable
import cn.revoist.lifephoton.module.funga.core.textmining.table.TextMiningTypeTable
import cn.revoist.lifephoton.tools.submit
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.limit
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import java.io.File
import java.util.UUID

/**
 * @author 6hisea
 * @date  2025/11/21 15:04
 * @description: None
 */
object TextMiningService {
    val splitter = DocumentByParagraphSplitter(3000,300)

    fun pushTask(file: File,type: String,userID:Long):String {
        val taskId = UUID.randomUUID().toString().replace("-","")
        submit(1,-1) {
            val document = DocumentEmbeder.parse(file)
            val texts = splitter.split(document)
            val res = FungaPlugin.dataManager.useDatabase()
                .from(TextMiningTypeTable)
                .select(TextMiningTypeTable.prompt,TextMiningTypeTable.format)
                .where {
                    TextMiningTypeTable.name eq type
                }.map {
                    Pair(
                        it.get(TextMiningTypeTable.prompt).toString(),
                        it.get(TextMiningTypeTable.format).toString()
                    )
                }.first()
            val result = HashMap<String, String>()
            texts.forEach {
                val res = TextMiningAssistant.INS.mining(it.text(),res.second,res.first)
                if (res != "[]"){
                    result.put(it.text(),res)
                }

            }
            FungaPlugin.dataManager.useDatabase()
                .insert(TextMiningTable){
                    set(it.task_id,taskId)
                    set(it.type,type)
                    set(it.user_id,userID)
                    set(it.result,result)
                }
        }
        return taskId
    }
    fun isReady(taskId: String):Boolean {
        return FungaPlugin.dataManager.useDatabase()
            .from(TextMiningTable)
            .select(TextMiningTable.task_id)
            .where {
                TextMiningTable.task_id eq taskId
            }.limit(1)
            .iterator().hasNext()
    }
    fun getResult(taskId: String): Map<String, String> {
        return FungaPlugin.dataManager.useDatabase()
            .from(TextMiningTable)
            .select(TextMiningTable.result)
            .where {
                TextMiningTable.task_id eq taskId
            }
            .limit(1)
            .map {
                it.get(TextMiningTable.result)
            }.first()!!
    }
    fun getHistory20(userID: Long):List<String> {
        return FungaPlugin.dataManager.useDatabase()
            .from(TextMiningTable)
            .select(TextMiningTable.task_id)
            .where {
                TextMiningTable.user_id eq userID
            }
            .limit(20)
            .map {
                it.getString(1)!!
            }
    }
}