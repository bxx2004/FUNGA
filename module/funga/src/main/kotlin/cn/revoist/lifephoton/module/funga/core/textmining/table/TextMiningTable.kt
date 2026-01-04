package cn.revoist.lifephoton.module.funga.core.textmining.table

import cn.revoist.lifephoton.plugin.anno.CreateTable
import cn.revoist.lifephoton.plugin.data.sqltype.obj
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/11/21 15:03
 * @description: None
 */
@CreateTable("funga", dbName = "funga", value = """
CREATE TABLE IF NOT EXISTS textmining (
    id BIGSERIAL PRIMARY KEY,
    task_id VARCHAR NOT NULL,
    type VARCHAR NOT NULL,
    result VARCHAR NOT NULL,
    user_id BIGINT NOT NULL
);
""")
object TextMiningTable : Table<Nothing>("textmining"){
    val id = long("id").primaryKey()
    val task_id = varchar("task_id")
    val type = varchar("type")
    val result = obj<Map<String, String>>("result")
    val user_id = long("user_id")
}