package cn.revoist.lifephoton.module.funga.core.textannotation.table

import cn.revoist.lifephoton.plugin.anno.CreateTable
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.double
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/11/10 15:25
 * @description: None
 */
@CreateTable("funga", dbName = "funga", value = """
CREATE TABLE IF NOT EXISTS text_annotation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    completion BOOLEAN NOT NULL,
    points DOUBLE PRECISION NOT NULL,
    date BIGINT NOT NULL,
    reason VARCHAR NOT NULL
);
""")
object TextAnnotationLogTable : Table<Nothing>("text_annotation_log") {
    val id = long("id").primaryKey()
    val user_id = long("user_id")
    val task_id = long("task_id")
    val completion = boolean("completion")
    val points = double("points")
    val date = long("date")
    val reason = varchar("reason")
}