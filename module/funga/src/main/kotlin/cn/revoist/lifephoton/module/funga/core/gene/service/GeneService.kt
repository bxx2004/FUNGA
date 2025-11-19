package cn.revoist.lifephoton.module.funga.core.gene.service

import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.data.database.vector.MilvusDatabase.search
import cn.revoist.lifephoton.module.funga.core.common.model.SentenceRequest
import cn.revoist.lifephoton.module.funga.data.table.GenePhenotypeTable
import cn.revoist.lifephoton.module.funga.data.table.GeneTable
import cn.revoist.lifephoton.module.funga.tools.asFungaId

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
            request.topK,
            request.minScore
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