package cn.revoist.lifephoton.module.funga.core.tools.table

import cn.revoist.lifephoton.plugin.anno.CreateTable
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/11/16 20:19
 * @description: None
 */
@CreateTable(
    "funga",
    dbName = "funga",
    value = """
CREATE TABLE IF NOT EXISTS alignment (
    id BIGSERIAL PRIMARY KEY,
    date BIGINT,
    analysis_id VARCHAR NOT NULL,
    result VARCHAR NOT NULL,
    species VARCHAR NOT NULL,
    type VARCHAR NOT NULL
);
    """
)
object AlignmentTable : Table<Nothing>("alignment")
{
    val id = long("id").primaryKey()
    val date = long("date")
    val analysis_id = varchar("analysis_id")
    val result = varchar("result")
    val species = varchar("species")
    val type = varchar("type")
}