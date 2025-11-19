package cn.revoist.lifephoton.module.funga.core.genegene.services

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.select
import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.core.common.model.GeneListWithDatabaseRequest
import cn.revoist.lifephoton.module.funga.core.common.model.GeneWithDatabasesRequest
import cn.revoist.lifephoton.module.funga.core.genephenotype.model.PredictGeneResponse
import cn.revoist.lifephoton.module.funga.data.table.GeneInteractionTable
import cn.revoist.lifephoton.module.funga.tools.asFungaId
import cn.revoist.lifephoton.module.funga.tools.asSymbol
import cn.revoist.lifephoton.module.funga.tools.tryMapping
import cn.revoist.lifephoton.plugin.data.maps
import cn.revoist.lifephoton.plugin.data.processor.MergeData
import cn.revoist.lifephoton.plugin.data.processor.join
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.dsl.or
import org.ktorm.dsl.where

/**
 * @author 6hisea
 * @date  2025/11/15 16:07
 * @description: None
 */
object GeneGeneService {
    fun getInteractionsByGeneList(request: GeneListWithDatabaseRequest): List<MergeData<List<HashMap<String, Any?>>>> {
        return join(request.dbs()){db->
            FungaPlugin.dataManager.useDatabase(db)
                .maps(GeneInteractionTable, GeneInteractionTable.id){
                    where {
                        (GeneInteractionTable.gene1 inList request.genes.asFungaId(db)) and (GeneInteractionTable.gene2 inList request.genes.asFungaId(db))
                    }
                }.tryMapping(db).let {
                    it.forEach {
                        it["references"] = (it["references"] as List<String>).distinct()
                    }
                    it
                }
        }
    }
    fun getInteractionsByPGR(pgr:PredictGeneResponse,db:String): HashMap<String, Any> {
        val allGenes = pgr.values.flatten().distinct()
        if (allGenes.isEmpty()) return HashMap()
        val degrees = HashMap<String,Int>()
        allGenes.forEach {
            degrees[it.asSymbol(db)] = pgr.findDegree(it)
        }
        return hashMapOf(
            "interactions" to FungaPlugin.dataManager.useDatabase(db)
                .maps(GeneInteractionTable, GeneInteractionTable.id){
                    where {
                        (GeneInteractionTable.gene1 inList allGenes) and (GeneInteractionTable.gene2 inList allGenes)
                    }
                }.tryMapping(db).let {
                    it.forEach {
                        it["summary"] = "interaction"
                    }
                    it
                },
            "genes" to degrees
        )
    }
    fun getInteractionsById(request: GeneWithDatabasesRequest): List<MergeData<List<HashMap<String, Any?>>>> {
        return join(request.dbs()){db->
            FungaPlugin.dataManager.useDatabase(db)
                .maps(GeneInteractionTable, GeneInteractionTable.id){
                    where {
                        (GeneInteractionTable.gene1 eq request.gene.asFungaId(db)) or (GeneInteractionTable.gene2 eq request.gene.asFungaId(db))
                    }
                }.tryMapping(db)
        }
    }
}