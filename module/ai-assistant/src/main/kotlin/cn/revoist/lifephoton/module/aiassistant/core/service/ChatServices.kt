package cn.revoist.lifephoton.module.aiassistant.core.service

import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import cn.revoist.lifephoton.module.aiassistant.AIAssistantAPI
import cn.revoist.lifephoton.module.aiassistant.core.entity.ChatOption
import cn.revoist.lifephoton.module.aiassistant.core.entity.HistorySummary
import cn.revoist.lifephoton.module.aiassistant.impl.chat.ChatAssistant
import cn.revoist.lifephoton.module.aiassistant.impl.db.computeTableIndex
import cn.revoist.lifephoton.module.aiassistant.impl.memory.MemoryTable
import cn.revoist.lifephoton.module.aiassistant.impl.model.ModelBaseEntity
import cn.revoist.lifephoton.module.aiassistant.impl.model.ModelTable
import cn.revoist.lifephoton.module.aiassistant.impl.rag.KnowledgeBaseAggregator
import cn.revoist.lifephoton.module.aiassistant.impl.role.RoleBaseEntity
import cn.revoist.lifephoton.module.aiassistant.impl.role.RoleTable
import cn.revoist.lifephoton.module.authentication.data.Tools
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.delete
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.update
import cn.revoist.lifephoton.plugin.data.sqltype.gson
import dev.langchain4j.service.TokenStream
import org.ktorm.dsl.and
import org.ktorm.dsl.delete
import org.ktorm.dsl.desc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.isNull
import org.ktorm.dsl.limit
import org.ktorm.dsl.map
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import java.util.UUID

/**
 * @author 6hisea
 * @date  2025/11/13 17:23
 * @description: None
 */
object ChatServices {
    fun getHistoriesId(user_id:Long):List<HistorySummary>{
        val table = MemoryTable.create(user_id.computeTableIndex())
        return AIAssistant.dataManager.useDatabase()
            .from(table)
            .select(table.memory_id,table.summary,table.update_date)
            .where {
                table.user_id eq user_id
            }.orderBy(table.update_date.desc())
            .map {
                HistorySummary(it.get(table.memory_id).toString(),it.get(table.summary).toString(),it.get(table.update_date) as Long)
            }
    }

    fun applyMemory(user_id: Long):String{
        val table = MemoryTable.create(user_id.computeTableIndex())
        val old = AIAssistant.dataManager.useDatabase()
            .from(table)
            .select(table.memory_id)
            .where {
                (table.user_id eq user_id) and (table.message.isNull())
            }.map {
                it.getString(1)!!
            }.firstOrNull()
        if (old != null) {
            return old
        }
        val id=UUID.randomUUID().toString()
        AIAssistant.dataManager.useDatabase()
            .insert(table){
                set(it.user_id, user_id)
                set(it.memory_id, id)
                set(it.update_date, System.currentTimeMillis())
                set(it.summary, "# New")
            }
        return id

    }

    fun deleteHistory(user_id:Long,memory_id: String):Boolean{
        val table = MemoryTable.create(user_id.computeTableIndex())
        AIAssistant.dataManager.useDatabase()
            .delete(table){
                 (it.user_id eq user_id) and (table.memory_id eq memory_id)
            }
        return true
    }
    fun clear(user_id:Long,memory_id: String):Boolean{
        val table = MemoryTable.create(user_id.computeTableIndex())
        AIAssistant.dataManager.useDatabase()
            .update(table){
                set(it.message, null)
                where {
                    (it.user_id eq user_id) and (table.memory_id eq memory_id)
                }
            }
        return true
    }
    fun getHistory(user_id:Long,memory_id: String):Any{
        val table = MemoryTable.create(user_id.computeTableIndex())
        return gson.fromJson(AIAssistant.dataManager.useDatabase()
            .from(table)
            .select(table.message)
            .where {
                (table.user_id eq user_id) and (table.memory_id eq memory_id)
            }
            .map {
                it.getString(1)!!
            }.first(),List::class.java)
    }
    fun chat(user_id: Long,option: ChatOption): TokenStream{
        val assistant = AIAssistantAPI.createAIAssistant(ChatAssistant::class.java,user_id, option)
        return assistant.chat(option.memoryId,option.message, KnowledgeBaseAggregator.options(user_id))
    }
    fun checkPermission(user_id: Long,option: ChatOption):Boolean{
        val model = AIAssistant.dataManager.useDatabase()
            .from(ModelTable)
            .select()
            .limit(1)
            .where {
                ModelTable.model_name eq option.modelName
            }.map {
                val entity = ModelBaseEntity()
                entity.model_name = it.get(ModelTable.model_name).toString()
                entity.permission = it.get(ModelTable.permission).toString()
                entity
            }.first()
        val role = AIAssistant.dataManager.useDatabase()
            .from(RoleTable)
            .select()
            .limit(1)
            .where {
                RoleTable.role_name eq option.role
            }.map {
                val entity = RoleBaseEntity()
                entity.role_name = it.get(RoleTable.role_name).toString()
                entity.permission = it.get(RoleTable.permission).toString()
                entity
            }.first()
        val permissions = Tools.getUserById(user_id)!!.permissions
        return (permissions.contains(role.permission) && permissions.contains(model.permission))
    }
    fun String.wrapper(type:String): ByteArray{
        return "$type:$this".encodeToByteArray()
    }
}