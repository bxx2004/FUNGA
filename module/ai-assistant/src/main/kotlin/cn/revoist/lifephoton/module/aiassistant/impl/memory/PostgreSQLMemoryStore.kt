package cn.revoist.lifephoton.module.aiassistant.impl.memory

import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import cn.revoist.lifephoton.module.aiassistant.AIAssistantAPI
import cn.revoist.lifephoton.module.aiassistant.impl.db.computeTableIndex
import cn.revoist.lifephoton.plugin.data.bind
import cn.revoist.lifephoton.tools.submit
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.ChatMessageDeserializer
import dev.langchain4j.data.message.ChatMessageSerializer
import dev.langchain4j.store.memory.chat.ChatMemoryStore
import org.ktorm.dsl.*
import org.ktorm.support.postgresql.insertOrUpdate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


/**
 * @author 6hisea
 * @date  2025/11/11 10:14
 * @description: None
 */
class PostgreSQLMemoryStore private constructor(val userId:Long) : ChatMemoryStore {
    val database = AIAssistant.dataManager.useDatabase()
    val table = MemoryTable.create(userId.computeTableIndex())
    private val summaryCache = hashMapOf<String, String>()
    override fun getMessages(memoryId: Any): List<ChatMessage> {
        val mappers = database.bind(table, MemoryMapper::class.java){
            select().where{
                (table.user_id eq userId) and (table.memory_id eq (memoryId as String))
            }
        }.firstOrNull()

        return if (mappers == null) {
            arrayListOf()
        }else{
            submit(1,-1) { if (!tasks.contains(mappers.memory_id)){
                tasks.add(mappers.memory_id)
                if (mappers.summary == null || mappers.summary == "# New" || mappers.summary.contains("无法")){
                    summaryCache[mappers.memory_id] = AIAssistantAPI.chatModel("qwen-flash")
                        .chat("请为下面的消息总结15到20个字的摘要，使用英文，不要使用系统消息，如果没有用户消息，输入# New即可:${mappers.message}")
                }
                tasks.remove(mappers.memory_id)
            } }
            ChatMessageDeserializer.messagesFromJson(mappers.message)
        }
    }

    private fun removeSingleResult(ori: List<ChatMessage>):List<ChatMessage>{
        if (ori.isEmpty()) {
            return ori
        }
        return ori
    }

    override fun updateMessages(
        memoryId: Any,
        messages: List<ChatMessage>
    ) {
        database.insertOrUpdate(table){
            set(it.memory_id, memoryId as String)
            set(it.user_id, userId)
            set(it.message, ChatMessageSerializer.messagesToJson(messages))
            set(it.update_date,System.currentTimeMillis())
            if (summaryCache.containsKey(memoryId)){
                set(it.summary, summaryCache[memoryId])
            }
            onConflict(it.memory_id){
                set(it.message, ChatMessageSerializer.messagesToJson(messages))
                if (summaryCache.containsKey(memoryId)){
                    set(it.summary, summaryCache[memoryId])
                }
            }
        }
    }

    override fun deleteMessages(memoryId: Any) {
        database.delete(table){
            (table.memory_id eq (memoryId as String)) and (table.user_id eq userId)
        }
    }
    companion object{

        private val tasks = CopyOnWriteArrayList<String>()

        private val cache = ConcurrentHashMap<Long, PostgreSQLMemoryStore>()
        fun create(userId:Long):PostgreSQLMemoryStore{
            return cache.computeIfAbsent(userId) { PostgreSQLMemoryStore(it) }
        }
        val lock = Any()
    }
}