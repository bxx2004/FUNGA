package cn.revoist.lifephoton.module.aiassistant.impl.rag

import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import cn.revoist.lifephoton.module.aiassistant.impl.db.createIfNotExists
import cn.revoist.lifephoton.plugin.anno.DynamicCreateTable
import io.ktor.util.collections.ConcurrentMap
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.bytes
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import java.util.concurrent.ConcurrentHashMap

/**
 * @author 6hisea
 * @date  2025/11/11 12:37
 * @description: None
 */
@DynamicCreateTable("""
    CREATE TABLE IF NOT EXISTS {{tableName}} (
    id BIGSERIAL PRIMARY KEY,
    doc_id VARCHAR NOT NULL,
    file BYTEA,
    embed BOOLEAN NOT NULL,
    date BIGINT NOT NULL,
    uploader BIGINT NOT NULL,
    citation VARCHAR NOT NULL,
    source VARCHAR NOT NULL,
    review BOOLEAN NOT NULL
);
""")
class DocumentTable private constructor(tableName: String) : Table<Nothing>("documents_${tableName}") {
    private constructor(tableIndex: Int): this(tableIndex.toString())
    init {
        createIfNotExists(AIAssistant)
    }
    val id = long("id").primaryKey()
    val doc_id = varchar("doc_id")
    val file = bytes("file")
    val embed = boolean("embed")
    val date = long("date")
    val uploader = long("uploader")
    val citation = varchar("citation")
    val source = varchar("source")
    val review = boolean("review")
    companion object{
        private val cache = ConcurrentHashMap<String, DocumentTable>()
        val public = DocumentTable("public")
        fun create(tableName: String):DocumentTable{
            return cache.computeIfAbsent(tableName) { DocumentTable(tableName) }
        }
        fun create(tableName: Int):DocumentTable{
            return cache.computeIfAbsent(tableName.toString()) { DocumentTable(tableName) }
        }
    }
}