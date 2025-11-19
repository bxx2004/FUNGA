package cn.revoist.lifephoton.module.funga.core.phenotype.service

import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.data.database.vector.MilvusDatabase.search
import cn.revoist.lifephoton.module.funga.core.phenotype.model.OntologyRequest
import cn.revoist.lifephoton.module.funga.core.phenotype.model.PhenotypeRequest
import cn.revoist.lifephoton.module.funga.data.table.GeneOntologyTable
import cn.revoist.lifephoton.module.funga.data.table.GenePhenotypeTable
import cn.revoist.lifephoton.module.funga.data.table.PhenotypeOntologyTable
import cn.revoist.lifephoton.module.funga.tools.tryMapping
import cn.revoist.lifephoton.plugin.data.maps
import cn.revoist.lifephoton.plugin.data.processor.MergeData
import cn.revoist.lifephoton.plugin.data.processor.join
import org.ktorm.dsl.eq
import org.ktorm.dsl.where

/**
 * @author 6hisea
 * @date  2025/4/19 11:55
 * @description: None
 */
object PhenotypeService {
    fun findPhenotypeOntologyByPhenotype(request:PhenotypeRequest): List<MergeData<List<Map<String, Any>>>> {
        request.replacePhenotype()
        return PhenotypeOntologyTable.search(
            request.dbs(),
            hashMapOf(
                Pair("description",request.phenotype)
            ),request.topK
            )
    }
    fun findGeneByPhenotype(request: PhenotypeRequest): List<MergeData<List<Map<String, Any>>>> {
        request.replacePhenotype()
        return GenePhenotypeTable.search(request.dbs(),hashMapOf(
            Pair("phenotype",request.phenotype)
        ),request.topK).tryMapping()
    }
    fun findGeneByPhenotypeOntology(request: OntologyRequest): List<MergeData<List<HashMap<String, Any?>>>> {
        return join(request.dbs()){db->
            FungaPlugin.dataManager.useDatabase(db)
                .maps(GenePhenotypeTable,GenePhenotypeTable.id){
                    where {
                        GenePhenotypeTable.phenotypeOntology eq request.ontology
                    }
                }.tryMapping(db)
        }
    }
    fun findGeneOntologyByPhenotype(request: PhenotypeRequest): List<Map<String, Any>> {
        request.replacePhenotype()
        return GeneOntologyTable.search("funga", hashMapOf(Pair("term",request.phenotype)),request.topK,request.minScore)
    }
}