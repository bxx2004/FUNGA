package cn.revoist.lifephoton.module.aiassistant.impl.role

import cn.revoist.lifephoton.plugin.anno.CreateTable
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/11/13 12:24
 * @description: None
 */
@CreateTable(plugin = "ai-assistant", dbName = "ai-assistant", value = """
    CREATE TABLE IF NOT EXISTS ai_role (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR NOT NULL,
    prompt VARCHAR NOT NULL,
    status BOOLEAN NOT NULL,
    permission VARCHAR NOT NULL
);
""")
object RoleTable : Table<Nothing>("ai_role") {
    val id = long("id").primaryKey()
    val role_name = varchar("role_name")
    val prompt = varchar("prompt")
    val status = boolean("status")
    val permission = varchar("permission")
}