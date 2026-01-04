package cn.revoist.lifephoton.module.funga.core.textmining.table

import cn.revoist.lifephoton.plugin.anno.CreateTable
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/11/21 15:03
 * @description: None
 */
@CreateTable("funga", dbName = "funga", value = """
    CREATE TABLE IF NOT EXISTS textmining_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    prompt VARCHAR NOT NULL,
    format VARCHAR NOT NULL
);
""")
object TextMiningTypeTable : Table<Nothing>("textmining_type"){
    val id = long("id").primaryKey()
    val name = varchar("name")
    val prompt = varchar("prompt")
    val format = varchar("format")
}