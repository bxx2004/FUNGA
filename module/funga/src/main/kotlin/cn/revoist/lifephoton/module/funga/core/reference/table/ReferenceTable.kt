package cn.revoist.lifephoton.module.funga.core.reference.table

import cn.revoist.lifephoton.plugin.anno.CreateTable
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/11/16 15:35
 * @description: None
 */

@CreateTable("funga", dbName = "funga", value = """
    CREATE TABLE IF NOT EXISTS "references" (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR NOT NULL,
    summary VARCHAR NOT NULL,
    citation VARCHAR NOT NULL,
    url VARCHAR NOT NULL
);
""")
object ReferenceTable : Table<Nothing>("references") {
    val id = long("id").primaryKey()
    val title = varchar("title")
    val summary = varchar("summary")
    val citation = varchar("citation")
    val url = varchar("url")
}