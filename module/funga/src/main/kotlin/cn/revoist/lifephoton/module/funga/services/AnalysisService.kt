package cn.revoist.lifephoton.module.funga.services

import cn.revoist.lifephoton.module.authentication.analysisTemplate
import cn.revoist.lifephoton.module.authentication.data.entity.UserDataEntity
import cn.revoist.lifephoton.module.authentication.data.table.hasFriend
import cn.revoist.lifephoton.module.authentication.pages.sendMessage
import cn.revoist.lifephoton.module.authentication.sendEmail
import cn.revoist.lifephoton.module.authentication.sendEmailNotice
import cn.revoist.lifephoton.module.filemanagement.FileManagementAPI
import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.ai.ModelStore
import cn.revoist.lifephoton.module.funga.ai.assistant.AnalysisAssistant
import cn.revoist.lifephoton.module.funga.ai.assistant.PaperAssistant
import cn.revoist.lifephoton.module.funga.ai.assistant.SortAssistant
import cn.revoist.lifephoton.module.funga.data.core.MilvusDatabase.search
import cn.revoist.lifephoton.module.funga.data.entity.ai.GeneResult
import cn.revoist.lifephoton.module.funga.data.entity.ai.SortedGeneResult
import cn.revoist.lifephoton.module.funga.data.entity.inneral.AnalysisResult
import cn.revoist.lifephoton.module.funga.data.entity.inneral.PredictGene
import cn.revoist.lifephoton.module.funga.data.entity.inneral.SearchContainer
import cn.revoist.lifephoton.module.funga.data.entity.mapper.GeneInteractionMapper
import cn.revoist.lifephoton.module.funga.data.entity.request.GeneListWithDatabaseRequest
import cn.revoist.lifephoton.module.funga.data.entity.request.ImputationFunGenesRequest
import cn.revoist.lifephoton.module.funga.data.entity.request.ImputationPredictGenesRequest
import cn.revoist.lifephoton.module.funga.data.entity.request.ImputationResultRequest
import cn.revoist.lifephoton.module.funga.data.entity.response.FuncGeneResponse
import cn.revoist.lifephoton.module.funga.data.entity.response.PhenotypeReferences
import cn.revoist.lifephoton.module.funga.data.entity.response.PredictGeneResponse
import cn.revoist.lifephoton.module.funga.data.table.GeneInteractionTable
import cn.revoist.lifephoton.module.funga.data.table.GenePhenotypeAnalysisTable
import cn.revoist.lifephoton.module.funga.data.table.GenePhenotypeTable
import cn.revoist.lifephoton.module.funga.data.table.type.AnalysisSummary
import cn.revoist.lifephoton.module.funga.tools.asFungaId
import cn.revoist.lifephoton.module.funga.tools.asSymbol
import cn.revoist.lifephoton.plugin.data.bind
import cn.revoist.lifephoton.plugin.data.mapsWithColumn
import cn.revoist.lifephoton.plugin.data.processor.MergeData
import cn.revoist.lifephoton.plugin.data.processor.join
import cn.revoist.lifephoton.plugin.data.sqltype.gson
import cn.revoist.lifephoton.tools.submit
import com.google.gson.JsonArray
import com.google.gson.stream.JsonReader
import io.ktor.server.util.toLocalDateTime
import kotlinx.coroutines.runBlocking
import org.ktorm.dsl.*
import java.io.FileReader
import java.util.Date
import kotlin.collections.set
import kotlin.random.Random

/**
 * @author 6hisea
 * @date  2025/4/19 16:19
 * @description: None
 */
object AnalysisService {
    val tempContainer = FungaPlugin.dataManager.useTempContainer<AnalysisResult>()
    fun computeAllFoundGene(f:List<MergeData<List<FuncGeneResponse>>>,o:List<MergeData<List<FuncGeneResponse>>>,i:List<MergeData<HashMap<String, Any>>>):List<String>{
        val list = ArrayList<String>()
        f.forEach {
            list.addAll(it.data.map { it.gene })
        }
        o.forEach {
            list.addAll(it.data.map { it.gene })
        }
        i.forEach {
            val genes = if (it.data["genes"] == null){
                hashMapOf<String, Int>()
            }else{
                it.data["genes"] as HashMap<String,Int>
            }
            list.addAll(genes.keys)
        }
        return list.distinct()
    }
    fun computeCount(inp:List<MergeData<List<FuncGeneResponse>>>):HashMap<String,Int>{
        val map = HashMap<String,Int>()
        inp.forEach {
            map[it.database] = it.data.size
        }
        return map
    }
    fun computeDegreeCount(inp:List<MergeData<HashMap<String, Any>>>):HashMap<String, HashMap<Int,Int>>{
        val map = HashMap<String, HashMap<Int,Int>>()
        inp.forEach {
            val genes = if (it.data["genes"] == null){
                hashMapOf<String, Int>()
            }else{
                it.data["genes"] as HashMap<String,Int>
            }
            val count =  HashMap<Int,Int>()
            genes.forEach {k,v->
                if (!count.containsKey(v)){
                    count[v] = 0
                }
                count[v] = count[v]!! + 1
            }
            map[it.database] = count
        }
        return map
    }
    fun nohupImputationFunGenes(request: ImputationFunGenesRequest,user:UserDataEntity): String {
        val id = (System.currentTimeMillis() + Random(1000).nextInt()).toString()
        submit(-1,-1){
            try {
                val startDate = Date().toString()
                val funcGenes = imputationFunGenes(request)
                val outerGenes = imputationOuterGene(request)
                val predictGenes = arrayListOf<MergeData<HashMap<String, Any>>>()
                funcGenes.forEach {fd->
                    val preReq = ImputationPredictGenesRequest()
                    preReq.geneList = request.genes
                    preReq.setDatabases(arrayListOf(fd.database))
                    preReq.genes = fd.data.map { it.gene }
                    preReq.degree = request.degree
                    predictGenes.add(imputationPredictGene(preReq).find { it.database == fd.database }!!)
                }
                val req = GeneListWithDatabaseRequest()
                req.setDatabases(request.dbs())
                val g1 = funcGenes.map { it.data.map { it.gene } }.flatten()
                val g2 = outerGenes.map { it.data.map { it.gene } }.flatten()
                val g3 = predictGenes.filter { it.data["genes"] != null }.map { (it.data["genes"] as HashMap<String,Int>).keys }.flatten()
                req.genes = g1 + g2 +g3

                val data = hashMapOf(
                    "func" to funcGenes,
                    "outer" to outerGenes,
                    "predict" to predictGenes,
                    "graph" to GeneService.getInteractionsByGeneList(req),
                )
                val summary = AnalysisSummary(
                    request.dbs(),request.genes,
                    request.phenotypes,request.type,
                    request.degree,request.topK,
                    computeCount(funcGenes),
                    computeDegreeCount(predictGenes),
                    computeCount(outerGenes),
                    computeAllFoundGene(funcGenes,outerGenes,predictGenes)
                    )
                val endDate = System.currentTimeMillis()
                FungaPlugin.dataManager.useDatabase()
                    .insert(GenePhenotypeAnalysisTable){
                        set(GenePhenotypeAnalysisTable.analysis_id,id)
                        set(GenePhenotypeAnalysisTable.result,gson.fromJson(gson.toJson(data), AnalysisResult::class.java))
                        set(GenePhenotypeAnalysisTable.user_id,user.id)
                        set(GenePhenotypeAnalysisTable.date, endDate)
                        set(GenePhenotypeAnalysisTable.summary,summary)
                    }

                runBlocking {
                    user.sendEmailNotice("【FUNGA-Analysis】Your analysis has been completed",analysisTemplate(
                        "【FUNGA-Analysis】Your analysis has been completed",
                        user.username,
                        id,
                        startDate,
                        Date(endDate).toString(),
                        "http://funga.revoist.cn/genePhenotypeDetail?id=${id}"
                    ))
                    user.sendMessage(
                        title = "Gene-Phenotype Analysis: Your analysis has been completed",
                        subtitle = "Click to view this message",
                        content = analysisTemplate(
                            "【FUNGA-Analysis】Your analysis has been completed",
                            user.username,
                            id,
                            startDate,
                            Date(endDate).toString(),
                            "http://funga.revoist.cn/genePhenotypeDetail?id=${id}")
                    )
                }
            }catch (e:Exception){
                e.printStackTrace()
                runBlocking {
                    user.sendEmail("【FUNGA-Analysis】Your analysis has been failed","reason：${e.toString()}.")
                    user.sendMessage(
                        title = "Gene-Phenotype Analysis: Your analysis has been failed",
                        subtitle = "Click to view this message",
                        content = "【FUNGA-Analysis】Your analysis has been failed\",\"reason：${e.toString()}.")
                }
            }
        }
        return id
    }

    fun imputationFunGenes(request:ImputationFunGenesRequest): List<MergeData<List<FuncGeneResponse>>> {
        return join(request.dbs()){db->
            val ids = request.genes.asFungaId(db)
            val phenotypes = HashMap<String,List<String>>()

            val funcGenes = arrayListOf<FuncGeneResponse>()

            FungaPlugin.dataManager.useDatabase(db)
                .mapsWithColumn(GenePhenotypeTable, GenePhenotypeTable.gene, GenePhenotypeTable.phenotype){
                    where {
                        GenePhenotypeTable.gene inList ids
                    }
                }.forEach {
                    if (!phenotypes.containsKey(it["gene"])){
                        phenotypes[it["gene"].toString()] = arrayListOf()
                    }
                    (phenotypes[it["gene"].toString()] as ArrayList<String>).add(it["phenotype"].toString())
                }
            phenotypes.forEach {gene,ps->
                var ppp: GeneResult
                //尝试新逻辑能否实现。
                //大量提示词一起发送。
                //判断Token长度
                //检验一下是否相关
                try {
                    val isRelated = if (request.type == "union"){
                        ppp = AnalysisAssistant.INSTANCE.isPhenotypeRelatedAny(request.phenotypes,ps)
                        ppp.result.isNotEmpty()
                    }else{
                        ppp = AnalysisAssistant.INSTANCE.isPhenotypeRelatedAll(request.phenotypes,ps)
                        ppp.result.isNotEmpty()
                    }
                    if(isRelated){
                        val result = FungaPlugin.dataManager.useDatabase(db)
                            .mapsWithColumn(GenePhenotypeTable, GenePhenotypeTable.phenotype,GenePhenotypeTable.references){
                                where {
                                    GenePhenotypeTable.gene eq gene and (GenePhenotypeTable.phenotype inList ppp.result.keys)
                                }
                            }.map {
                                PhenotypeReferences(it["phenotype"].toString(),(it["references"] as List<String>).distinct(),ppp.result[it["phenotype"].toString()].toString())
                            }
                        funcGenes.add(FuncGeneResponse(gene.asSymbol(db),result,ppp.summary))
                    }
                }catch (e:Exception){
                    println("检查异常")
                    e.printStackTrace()
                }
            }
            funcGenes
        }
    }
    fun imputationPredictGene(request: ImputationPredictGenesRequest,currentDegree:Int = 1): List<MergeData<HashMap<String, Any>>> {
        val fGene = request.genes.toList()
        return join(request.dbs()){db->
            val filterRes = PredictGeneResponse()
            for ((degree,genes) in findInteractions(db,request,currentDegree)){
                filterRes[degree] = genes.filter {
                    !fGene.asFungaId(db).contains(it)
                }
            }
            GeneService.getInteractionsByPGR(filterRes,db)
        }
    }
    private fun findInteractions(db: String, request: ImputationPredictGenesRequest, currentDegree: Int): PredictGeneResponse {
        if (request.genes.isEmpty()) return PredictGeneResponse()

        val genes = request.genes.asFungaId(db)
        val geneList = request.geneList.asFungaId(db)

        val dg1 = PredictGeneResponse()

        // 分批处理大列表
        val batchSize = 10000
        val resultSet = mutableSetOf<String>()

        // 处理 geneList 分批
        geneList.chunked(batchSize) { geneListBatch ->
            // 处理 genes 分批
            genes.chunked(batchSize) { genesBatch ->
                resultSet.addAll(
                    FungaPlugin.dataManager.useDatabase(db)
                        .bind(GeneInteractionTable, GeneInteractionMapper::class.java) {
                            select(GeneInteractionTable.gene1, GeneInteractionTable.gene2)
                                .where {
                                    // 使用分批后的列表
                                    ((GeneInteractionTable.gene1 inList geneListBatch) and
                                            (GeneInteractionTable.gene2 inList geneListBatch)) and
                                            ((GeneInteractionTable.gene1 inList genesBatch) or
                                                    (GeneInteractionTable.gene2 inList genesBatch))
                                }
                        }.map {
                            if (it.gene1 in request.genes) {
                                it.gene2
                            } else {
                                it.gene1
                            }
                        }
                )
            }
        }

        dg1[currentDegree] = resultSet.toList()

        return if (currentDegree <= request.degree) {
            request.genes = dg1[currentDegree]!!
            findInteractions(db, request, currentDegree + 1) + dg1
        } else {
            dg1
        }
    }
    fun imputationOuterGene(request:ImputationFunGenesRequest): List<MergeData<List<FuncGeneResponse>>> {
        return join(request.dbs()){db->
            val result = HashMap<String,ArrayList<PhenotypeReferences>>()
            val ids = request.genes.asFungaId(db)
            request.phenotypes.forEach {
                GenePhenotypeTable.search(db, hashMapOf("phenotype" to it),request.topK).forEach {
                    if (!ids.contains(it["gene"])){
                        if (!result.containsKey(it["gene"])){
                            result[it["gene"].toString()] = arrayListOf()
                        }
                        result[it["gene"]]?.add(PhenotypeReferences(it["phenotype"].toString(),(it["references"] as JsonArray).map { it.asString }.distinct(),"causality"))
                    }
                }
            }

            return@join result.map {
                FuncGeneResponse(it.key.asSymbol(db),it.value,"causality")
            }.filter { it.phenotypes.isNotEmpty() }
        }
    }
    fun isReadyImputation(id:String):Boolean{
        return FungaPlugin.dataManager.useDatabase()
            .from(GenePhenotypeAnalysisTable)
            .select(GenePhenotypeAnalysisTable.analysis_id)
            .where {
                GenePhenotypeAnalysisTable.analysis_id eq id
            }.asIterable().toList().isNotEmpty()
    }
    fun getImputation(request:ImputationResultRequest,user: UserDataEntity):Any{
        val dbs = request.dbs()
        val data = FungaPlugin.dataManager.useDatabase()
            .from(GenePhenotypeAnalysisTable)
            .select()
            .where {
                GenePhenotypeAnalysisTable.analysis_id eq request.id
            }.asIterable().firstOrNull()
        if (data == null) {
            return "Not found data."
        }
        if (!data[GenePhenotypeAnalysisTable.user_id]!!.hasFriend(user.id) && data[GenePhenotypeAnalysisTable.user_id]!! != user.id) {
            return "You don't have permission to look this data."
        }
        val result = data[GenePhenotypeAnalysisTable.result]!!

        return when(request.type){
            "discussion" -> {
                if (request.genes.isEmpty()){
                    "Please select gene."
                }else{
                    val func = try {
                        result.func.find { it.database == request.dbs()[0] }!!.data!!.filter {
                            request.genes.contains(it.gene)
                        }
                    }catch (e:Exception){
                        "无"
                    }
                    val outer = try {
                        result.outer.find { it.database == request.dbs()[0] }!!.data!!.filter {
                            request.genes.contains(it.gene)
                        }
                    }catch (e:Exception){
                        "无"
                    }
                    val network = try {
                        result.predict.find { it.database == request.dbs()[0] }!!.data!!.interactions.filter {
                            request.genes.contains(it.gene1) || request.genes.contains(it.gene2)
                        }
                    }catch (e:Exception){
                        "无"
                    }
                    PaperAssistant.INSTANCE.generateDiscretion(gson.toJson(hashMapOf(
                        "func" to func,
                        "outer" to outer,
                        "network" to network,
                    )),request.prompt)
                }
            }
            "graph" -> {
                result.graph.map { it.data!! }.flatten().filter {
                    it.gene1 in request.genes && it.gene2 in request.genes
                }
            }
            "input"-> {
                data[GenePhenotypeAnalysisTable.summary]!!
            }
            "func" -> {
                result.func.filter { dbs.contains(it.database) }
            }
            "outer" -> {
                result.outer.filter { dbs.contains(it.database) }
            }
            "predict" -> {
                val r = arrayListOf<SearchContainer<List<PredictGene>>>()
                result.predict.filter { dbs.contains(it.database) }.forEach {
                    val nsc = SearchContainer<List<PredictGene>>()
                    nsc.database = it.database
                    nsc.data = arrayListOf()
                    val pg = PredictGene()
                    val m = it.data!!.genes.filter { it.value <= request.degree }

                    val mm = HashMap<String,Int>()
                    mm.putAll(m)
                    mm.keys.forEach {
                        mm[it] = mm[it]!!
                    }
                    pg.genes = mm
                    pg.interactions = it.data!!.interactions.filter { it.gene1 in mm.keys || it.gene2 in mm.keys }
                    (nsc.data as ArrayList<PredictGene>).add(pg)
                    r.add(nsc)
                }
                r
            }
            else->{
                result.func.filter { dbs.contains(it.database) }
            }
        }
    }
    fun funcGeneSort(geneData: Map<String, List<String>>,phenotype: String,db: String): SortedGeneResult {
        return SortAssistant.INSTANCE.sort(geneData,phenotype,db)
    }
}