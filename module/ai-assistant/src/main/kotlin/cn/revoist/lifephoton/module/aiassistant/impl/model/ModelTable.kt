package cn.revoist.lifephoton.module.aiassistant.impl.model

import cn.revoist.lifephoton.plugin.anno.CreateTable
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/11/13 17:34
 * @description: None
 */
@CreateTable(plugin = "ai-assistant", dbName = "ai-assistant", value = """
    CREATE TABLE IF NOT EXISTS ai_model (
    id BIGSERIAL PRIMARY KEY,
    model_name VARCHAR NOT NULL,
    permission VARCHAR NOT NULL
);
""")
object ModelTable : Table<Nothing>("ai_model") {
    val id = long("id").primaryKey()
    val model_name = varchar("model_name")
    val permission = varchar("permission")
}