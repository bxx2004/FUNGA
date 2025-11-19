package cn.revoist.lifephoton.module.aiassistant.impl.memory

import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import cn.revoist.lifephoton.module.aiassistant.impl.db.computeTableIndex
import cn.revoist.lifephoton.module.aiassistant.impl.db.createIfNotExists
import cn.revoist.lifephoton.module.aiassistant.impl.rag.DocumentTable
import cn.revoist.lifephoton.plugin.anno.DynamicCreateTable
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import java.util.concurrent.ConcurrentHashMap

/**
 * @author 6hisea
 * @date  2025/11/11 10:35
 * @description: None
 */
@DynamicCreateTable(
    """
CREATE TABLE IF NOT EXISTS {{tableName}} (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    memory_id VARCHAR UNIQUE,
    message VARCHAR,
    summary VARCHAR,
    update_date BIGINT NOT NULL
);
    """
)
class MemoryTable private constructor(tableIndex: Int): Table<Nothing>("user_memory_${tableIndex}") {
    init {
        createIfNotExists(AIAssistant)
    }
    val id = long("id").primaryKey()
    val user_id = long("user_id")
    val memory_id = varchar("memory_id")
    val message = varchar("message")
    val summary = varchar("summary")
    val update_date = long("update_date")
    companion object{
        private val cache = ConcurrentHashMap<Int, MemoryTable>()
        fun create(tableName: Int):MemoryTable{
            return cache.computeIfAbsent(tableName) { MemoryTable(tableName) }
        }
    }
}