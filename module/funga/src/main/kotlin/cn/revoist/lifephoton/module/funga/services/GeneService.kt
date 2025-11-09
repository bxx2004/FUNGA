package cn.revoist.lifephoton.module.funga.services

import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.data.core.MilvusDatabase.search
import cn.revoist.lifephoton.module.funga.data.entity.request.GeneListWithDatabaseRequest
import cn.revoist.lifephoton.module.funga.data.entity.request.GeneWithDatabasesRequest
import cn.revoist.lifephoton.module.funga.data.entity.request.SentenceRequest
import cn.revoist.lifephoton.module.funga.data.entity.response.PredictGeneResponse
import cn.revoist.lifephoton.module.funga.data.table.GeneInteractionTable
import cn.revoist.lifephoton.module.funga.data.table.GenePhenotypeTable
import cn.revoist.lifephoton.module.funga.data.table.GeneTable
import cn.revoist.lifephoton.module.funga.tools.asFungaId
import cn.revoist.lifephoton.module.funga.tools.asSymbol
import cn.revoist.lifephoton.module.funga.tools.tryMapping

import cn.revoist.lifephoton.plugin.data.maps
import cn.revoist.lifephoton.plugin.data.processor.MergeData
import cn.revoist.lifephoton.plugin.data.processor.join
import org.ktorm.dsl.*

/**
 * @author 6hisea
 * @date  2025/4/13 14:17
 * @description: None
 */
object GeneService {
    fun searchGeneBySentence(request: SentenceRequest): List<MergeData<List<Map<String, Any>>>> {
        return GeneTable.search(request.dbs(),
            mapOf(
                Pair("description",request.sentence)
            ),
            request.topK
        )
    }
    fun getPhenotypesById(gene:String,dbs:List<String>): List<MergeData<List<HashMap<String, Any?>>>> {
        return join(dbs){
            FungaPlugin.dataManager.useDatabase(it)
                .maps(GenePhenotypeTable, GenePhenotypeTable.id){
                    where {
                        GenePhenotypeTable.gene eq gene.asFungaId(it)
                    }
                }
        }
    }
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
    fun getInformationById(gene:String,dbs:List<String>): List<MergeData<List<HashMap<String, Any?>>>> {
        return join(dbs){db->
            FungaPlugin.dataManager.useDatabase(db)
                .maps(GeneTable, GeneTable.id){
                    where {
                        GeneTable.fungaId eq gene.asFungaId(db)
                    }
                }
        }
    }
}