package cn.revoist.lifephoton.module.funga.data.table

import cn.revoist.lifephoton.module.funga.data.art
import cn.revoist.lifephoton.module.funga.data.table.type.AnalysisSummary
import cn.revoist.lifephoton.plugin.anno.CreateTable
import cn.revoist.lifephoton.plugin.data.sqltype.obj
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/8/24 15:22
 * @description: None
 */
@CreateTable(
    plugin = "funga",
    dbName = "funga",
    value = """
CREATE TABLE IF NOT EXISTS gene_phenotype_analysis (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    analysis_id VARCHAR UNIQUE,
    summary VARCHAR,
    result VARCHAR,
    date BIGINT NOT NULL
);
    """
)
object GenePhenotypeAnalysisTable : Table<Nothing>("gene_phenotype_analysis"){
    val id = int("id").primaryKey()
    val analysis_id = varchar("analysis_id")
    val user_id = long("user_id")
    val date = long("date")
    val result = art("result")
    val summary = obj<AnalysisSummary>("summary")
}