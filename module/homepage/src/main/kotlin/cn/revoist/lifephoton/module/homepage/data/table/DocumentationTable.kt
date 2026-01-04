package cn.revoist.lifephoton.module.homepage.data.table

import cn.revoist.lifephoton.plugin.anno.CreateTable
import cn.revoist.lifephoton.plugin.data.sqltype.obj
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/11/20 11:07
 * @description: None
 */
@CreateTable("lifephoton", value = """
    CREATE TABLE IF NOT EXISTS documentation (
    id BIGSERIAL PRIMARY KEY,
    doc_id VARCHAR NOT NULL,
    title VARCHAR NOT NULL,
    update_time BIGINT NOT NULL,
    content VARCHAR NOT NULL,
    children VARCHAR NOT NULL
);
""")
object DocumentationTable : Table<Nothing>("documentation") {
    val id = long("id").primaryKey()
    val doc_id = varchar("doc_id")
    val title = varchar("title")
    val update_time = long("update_time")
    val content = varchar("content")
    val children = obj<List<String>>("children")
}