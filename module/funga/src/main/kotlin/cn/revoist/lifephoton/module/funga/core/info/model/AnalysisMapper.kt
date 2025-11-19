package cn.revoist.lifephoton.module.funga.core.info.model

import cn.revoist.lifephoton.module.funga.core.genephenotype.model.AnalysisResult
import cn.revoist.lifephoton.module.funga.data.table.type.AnalysisSummary

/**
 * @author 6hisea
 * @date  2025/11/16 12:45
 * @description: None
 */
class AnalysisMapper {
    val id = -1L
    lateinit var analysis_id:String
    val user_id = -1L
    val date = -1L
    lateinit var result: AnalysisResult
    lateinit var summary:AnalysisSummary
}