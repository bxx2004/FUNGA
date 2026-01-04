package cn.revoist.lifephoton.module.genome.service

import cn.revoist.lifephoton.module.genome.Genome
import cn.revoist.lifephoton.module.genome.GenomeAPI
import cn.revoist.lifephoton.module.genome.data.entity.request.GeneInfoRequest
import cn.revoist.lifephoton.module.genome.data.entity.response.GeneAnnotationResponse
import cn.revoist.lifephoton.module.genome.data.table.GenomeGeneInfo
import cn.revoist.lifephoton.plugin.data.buildWhere
import cn.revoist.lifephoton.plugin.data.maps
import cn.revoist.lifephoton.plugin.property
import org.ktorm.dsl.*
import org.ktorm.schema.Column
import org.ktorm.schema.ColumnDeclaring

/**
 * @author 6hisea
 * @date  2025/1/25 10:54
 * @description: None
 */
object GeneBaseService {
    fun getGeneAnnotationByGeneId(request: cn.revoist.lifephoton.module.genome.data.entity.request.GeneBasicRequest):List<GeneAnnotationResponse>{
        val response = ArrayList<GeneAnnotationResponse>()
        GenomeAPI.useAnnotationTables().forEach { table->
            val res = GeneAnnotationResponse()
            res.type = table.tableName.split("_").last().uppercase()
            res.data = Genome.dataManager.useDefaultDatabase()
                .maps(table,table.property("id")!!.get(table) as Column<*>){
                    where {
                        buildWhere {
                            append {
                                (table.property("geneId")!!.get(table) as Column<String>) eq request.geneId
                            }
                            appendIf(request.speciesName != null){
                                (table.property("speciesName")!!.get(table) as Column<String>) eq request.speciesName!!
                            }
                            appendIf(request.assembledVersion != null){
                                (table.property("assembledVersion")!!.get(table) as Column<String>) eq request.assembledVersion!!
                            }
                        }
                    }
                }
            response.add(res)
        }
        val cache = HashMap<String, GeneAnnotationResponse>()
        Genome.dataManager.useDefaultDatabase()
            .maps(cn.revoist.lifephoton.module.genome.data.table.GenomeGeneAnnotationT, cn.revoist.lifephoton.module.genome.data.table.GenomeGeneAnnotationT.id){
                where {
                    buildWhere {
                        append {
                            cn.revoist.lifephoton.module.genome.data.table.GenomeGeneAnnotationT.geneId eq request.geneId
                        }
                        appendIf(request.speciesName != null){
                            cn.revoist.lifephoton.module.genome.data.table.GenomeGeneAnnotationT.speciesName eq request.speciesName!!
                        }
                        appendIf(request.assembledVersion != null){
                            cn.revoist.lifephoton.module.genome.data.table.GenomeGeneAnnotationT.assembledVersion eq request.assembledVersion!!
                        }
                    }
                }
            }
            .forEach {
                if (!cache.containsKey(it["source"])){
                    cache[it["source"].toString()] = GeneAnnotationResponse()
                    cache[it["source"].toString()]!!.type = it["source"].toString().uppercase()
                    cache[it["source"].toString()]!!.data = ArrayList<HashMap<String,Any?>>()
                }
                (cache[it["source"].toString()]!!.data as ArrayList<HashMap<String,Any?>>)
                    .add(
                        it
                    )
            }
        cache.forEach { (_, u) ->
            response.add(u)
        }
        return response
    }

    fun searchGeneInfoByGeneInfoRequest(request: GeneInfoRequest) : List<HashMap<String,Any?>>{
        return Genome.dataManager.useDefaultDatabase()
            .maps(GenomeGeneInfo, GenomeGeneInfo.id){
                where {
                    val conditions = ArrayList<ColumnDeclaring<Boolean>>()
                    when (request.type){
                        "id" -> {
                            conditions += GenomeGeneInfo.geneId eq request.data["gene_id"].toString()
                        }
                        "region" ->{
                            conditions += GenomeGeneInfo.start greaterEq request.data["start"].toString().toDouble().toLong()
                            conditions += GenomeGeneInfo.end lessEq request.data["end"].toString().toDouble().toLong()
                            if (request.data.containsKey("locus")){
                                conditions += GenomeGeneInfo.locus eq request.data["locus"].toString()
                            }
                        }

                        "name" -> {
                            conditions += GenomeGeneInfo.geneName like "%" + request.data["gene_name"].toString() + "%"
                        }
                    }
                    if (request.data.containsKey("species_name")){
                        conditions += GenomeGeneInfo.speciesName eq request.data["species_name"].toString()
                    }
                    if (request.data.containsKey("assembled_version")){
                        conditions += GenomeGeneInfo.assembledVersion eq request.data["assembled_version"].toString()
                    }
                    conditions.reduce { a, b -> a and b }
                }
            }
    }
}