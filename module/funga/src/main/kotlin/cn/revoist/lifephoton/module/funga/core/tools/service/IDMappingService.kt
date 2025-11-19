package cn.revoist.lifephoton.module.funga.core.tools.service

import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.core.common.model.GeneListWithDatabaseRequest
import cn.revoist.lifephoton.module.funga.data.table.GeneTable
import cn.revoist.lifephoton.module.funga.tools.asFungaId
import cn.revoist.lifephoton.plugin.data.mapsWithColumn
import cn.revoist.lifephoton.plugin.data.processor.MergeData
import cn.revoist.lifephoton.plugin.data.processor.join
import org.ktorm.dsl.inList
import org.ktorm.dsl.where

/**
 * @author 6hisea
 * @date  2025/5/28 20:30
 * @description: None
 */
object IDMappingService {

    fun idMapping(req: GeneListWithDatabaseRequest):List<MergeData<List<HashMap<String, Any?>>>>{
         return join(req.dbs()) { db ->
             val genes = req.genes.asFungaId(db)
             return@join FungaPlugin.dataManager.useDatabase(db)
                 .mapsWithColumn(GeneTable, GeneTable.fungaId, GeneTable.symbol, GeneTable.otherId) {
                     where {
                         GeneTable.fungaId inList genes
                     }
                 }
         }
    }
}