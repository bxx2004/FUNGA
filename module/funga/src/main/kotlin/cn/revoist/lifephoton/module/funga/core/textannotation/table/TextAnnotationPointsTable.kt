package cn.revoist.lifephoton.module.funga.core.textannotation.table

import cn.revoist.lifephoton.plugin.anno.CreateTable
import org.ktorm.schema.Table
import org.ktorm.schema.double
import org.ktorm.schema.long

/**
 * @author 6hisea
 * @date  2025/11/10 15:25
 * @description: None
 */
@CreateTable("funga", dbName = "funga", value = """
CREATE TABLE IF NOT EXISTS text_annotation_points (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    points DOUBLE PRECISION NOT NULL DEFAULT 0.0
);
""")
object TextAnnotationPointsTable : Table<Nothing>("text_annotation_points") {
    val id = long("id").primaryKey()
    val user_id = long("user_id")
    val points = double("points")
}