package cn.revoist.lifephoton.module.funga.core.textannotation.table

import cn.revoist.lifephoton.module.funga.core.textannotation.model.TextAnnotationResult
import cn.revoist.lifephoton.module.funga.data.table.type.Source
import cn.revoist.lifephoton.plugin.anno.CreateTable
import cn.revoist.lifephoton.plugin.data.sqltype.obj
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/11/10 15:25
 * @description: None
 */
@CreateTable("funga", dbName = "funga", value = """
    CREATE TABLE IF NOT EXISTS text_annotation_dirty (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR NOT NULL,
    text VARCHAR NOT NULL,
    result VARCHAR NOT NULL,
    taxonomy VARCHAR NOT NULL,
    completion BOOLEAN NOT NULL,
    completion_user BIGINT NOT NULL,
    completion_date BIGINT NOT NULL,
    reason VARCHAR NOT NULL
);
""")
object TextAnnotationDirtyTable : Table<Nothing>("text_annotation_dirty") {
    val id = long("id").primaryKey()
    val source = obj<Source>("source")
    val text = varchar("text")
    val result = obj<TextAnnotationResult>("result")
    val taxonomy = varchar("taxonomy")
    val completion = boolean("completion")
    val completion_user = long("completion_user")
    val completion_date = long("completion_date")
    val reason = varchar("reason")
}