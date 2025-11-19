package cn.revoist.lifephoton.module.homepage.data.table

import cn.revoist.lifephoton.plugin.anno.CreateTable
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/11/7 18:23
 * @description: None
 */
@CreateTable("homepage","""
    CREATE TABLE IF NOT EXISTS bar (
    id BIGSERIAL PRIMARY KEY,
    image VARCHAR NOT NULL,
    url VARCHAR NOT NULL
);
""")
object BarTable : Table<Nothing>("bar") {
    val id = long("id").primaryKey()
    val image = varchar("image")
    val url = varchar("url")
}