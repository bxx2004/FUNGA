package cn.revoist.lifephoton.module.authentication.data.table

import cn.revoist.lifephoton.plugin.anno.CreateTable
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/8/24 15:49
 * @description: None
 */
@CreateTable("auth", dbName = "auth", value = """
CREATE TABLE if not exists message (
    id SERIAL PRIMARY KEY,
    title VARCHAR NOT NULL,
    subtitle VARCHAR NOT NULL,
    "from" BIGINT NOT NULL,
    "to" BIGINT NOT NULL,
    content VARCHAR NOT NULL,
    read BOOLEAN NOT NULL,
    timestamp BIGINT NOT NULL
);
""")
object MessageTable : Table<Nothing>("message") {
    val id = long("id").primaryKey()
    val title = varchar("title")
    val subtitle = varchar("subtitle")
    val from = long("from")
    val to = long("to")
    val content = varchar("content")
    val read = boolean("read")
    val timestamp = long("timestamp")
}