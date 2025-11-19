package cn.revoist.lifephoton.module.funga.core.info.service

import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.core.info.model.AnalysisMapper
import cn.revoist.lifephoton.module.funga.core.info.model.DBInfoBaseEntity
import cn.revoist.lifephoton.module.funga.core.info.model.DBInfoMapper
import cn.revoist.lifephoton.module.funga.core.info.table.DBInfoTable
import cn.revoist.lifephoton.module.funga.data.table.GenePhenotypeAnalysisTable
import cn.revoist.lifephoton.plugin.data.bind
import cn.revoist.lifephoton.plugin.data.toMap
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.select
import org.ktorm.dsl.where

/**
 * @author 6hisea
 * @date  2025/11/15 16:29
 * @description: None
 */
object InfoService {
    fun getAllDatabase():List<DBInfoMapper>{
        return FungaPlugin.dataManager.useDatabase()
            .bind(DBInfoTable, DBInfoMapper::class.java){
                select()
            }
    }
    fun getAllAnalysisResult(id:Long):List<AnalysisMapper>{
        return FungaPlugin.dataManager.useDatabase()
            .bind(GenePhenotypeAnalysisTable, AnalysisMapper::class.java){
                select()
                    .where {
                        GenePhenotypeAnalysisTable.user_id eq id
                    }
            }
    }
    fun getAllAnalysisSummary(user:Long):List<Map<String,Any?>>{
        return FungaPlugin.dataManager.useDatabase()
            .from(GenePhenotypeAnalysisTable)
            .select(GenePhenotypeAnalysisTable.analysis_id, GenePhenotypeAnalysisTable.date,GenePhenotypeAnalysisTable.summary)
            .where {
                GenePhenotypeAnalysisTable.user_id eq user
            }.toMap(GenePhenotypeAnalysisTable)
    }
}