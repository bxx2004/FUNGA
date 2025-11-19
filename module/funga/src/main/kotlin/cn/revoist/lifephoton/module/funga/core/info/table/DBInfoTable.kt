package cn.revoist.lifephoton.module.funga.core.info.table

import cn.revoist.lifephoton.plugin.anno.CreateTable
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/4/13 13:00
 * @description: None
 */
@CreateTable("funga", dbName = "funga", value = """
CREATE TABLE IF NOT EXISTS db_info (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    taxonomy VARCHAR NOT NULL,
    description VARCHAR NOT NULL,
    image VARCHAR NOT NULL
);
""")
object DBInfoTable : Table<Nothing>("db_info") {
    val id = int("id").primaryKey()
    val name = varchar("name")
    val taxonomy = varchar("taxonomy")
    val description = varchar("description")
    val image = varchar("image")
}