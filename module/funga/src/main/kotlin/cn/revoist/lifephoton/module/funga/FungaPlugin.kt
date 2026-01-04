package cn.revoist.lifephoton.module.funga

import cn.revoist.lifephoton.module.funga.data.database.vector.MilvusDatabase
import cn.revoist.lifephoton.module.funga.core.info.table.DBInfoTable
import cn.revoist.lifephoton.module.funga.data.table.GeneInteractionTable
import cn.revoist.lifephoton.module.funga.data.table.GenePhenotypeTable
import cn.revoist.lifephoton.module.funga.data.table.GeneTable
import cn.revoist.lifephoton.module.funga.tools.asFungaId
import cn.revoist.lifephoton.module.funga.tools.asSymbol
import cn.revoist.lifephoton.module.funga.tools.fungaIdMapCache
import cn.revoist.lifephoton.module.funga.tools.symbolMapCache
import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.anno.AutoUse
import cn.revoist.lifephoton.plugin.data.maps
import cn.revoist.lifephoton.plugin.data.mapsWithColumn
import cn.revoist.lifephoton.plugin.data.sqltype.gson
import org.ktorm.dsl.and
import org.ktorm.dsl.from
import org.ktorm.dsl.inList
import org.ktorm.dsl.like
import org.ktorm.dsl.map
import org.ktorm.dsl.or
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import java.io.File

/**
 * @author 6hisea
 * @date  2025/4/13 11:24
 * @description: None
 */
@AutoUse
object FungaPlugin : Plugin(){
    override val name: String
        get() = "FUNGA"
    override val author: String
        get() = "Haixu Liu"
    override val version: String
        get() = "beta-1"

    val diamondExec = "/data/LifePhoton/funga/exec/${
        if (getOS() == OS.WINDOWS){
            "diamond.exe"
        }else{
            "diamond"
        }
    }"
    fun dmnd(child:String): File{
        return File("/data/LifePhoton/funga/dmnd/${child}.dmnd")
    }

    override fun load() {
        MilvusDatabase.init()
        val dir = File("/data/LifePhoton/funga")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val dbs = dataManager.useDatabase().maps(DBInfoTable,DBInfoTable.id).map { it["name"].toString() }
        dbs.forEach {db->
            dataManager.useDatabase(db).mapsWithColumn(
                GeneTable, GeneTable.fungaId, GeneTable.symbol, GeneTable.otherId
            ).forEach {
                val symbol = if (it["symbol"] == null){
                    (it["other_id"] as List<String>)[0]
                }else if (it["symbol"] == "None"){
                    (it["other_id"] as List<String>)[0]
                }else{
                    it["symbol"].toString()
                }
                val fungaId = it["funga_id"] as String
                if (!symbolMapCache.containsKey(db)){
                    symbolMapCache[db] = hashMapOf()
                }
                if (!fungaIdMapCache.containsKey(db)){
                    fungaIdMapCache[db] = hashMapOf()
                }
                symbolMapCache[db]!![fungaId] = symbol
                if (it["other_id"] != null){
                    (it["other_id"] as List<String>).forEach {
                        fungaIdMapCache[db]!![it] = fungaId
                    }
                }
                fungaIdMapCache[db]!![symbol] = fungaId
            }
        }
    }

    private fun filterNeg(){
        val pos = File("C:\\Users\\12232\\Desktop\\FUNGA\\阴阳\\pos.csv").readLines()
        val neg = File("C:\\Users\\12232\\Desktop\\FUNGA\\阴阳\\neg.csv").readLines()

        val posFungaId = hashMapOf<String, String>()
        val negFungaId = hashMapOf<String, String>()

        pos.forEach {
            posFungaId[it] = it.asFungaId("saccharomyces")
        }
        neg.forEach {
            negFungaId[it] = it.asFungaId("saccharomyces")
        }

        val f = File("C:\\Users\\12232\\Desktop\\FUNGA\\阴阳\\f.csv")
        val res = dataManager.useDatabase("saccharomyces")
            .from(GeneInteractionTable)
            .select(GeneInteractionTable.gene1, GeneInteractionTable.gene2)
            .where { (GeneInteractionTable.gene1 inList posFungaId.values) or (GeneInteractionTable.gene2 inList posFungaId.values) }
            .map {
                val gene1 = it[GeneInteractionTable.gene1].toString()
                val gene2 = it[GeneInteractionTable.gene2].toString()
                listOf(gene1,gene2)
            }.flatten().filter { it !in posFungaId.values }.distinct()
        f.writeText(res.map { it.asSymbol("saccharomyces") }.joinToString("\n"))
    //f.writeText(negFungaId.values.filter { it !in res }.map { r-> negFungaId.filter { it.value == r }.keys.first() }.filter { it !in pos }.joinToString("\n"))
    }
    private fun generateData() {
        val genes = listOf("DAK2").asFungaId("saccharomyces")
        val phe = "%mitotic recombination%"

        // 预获取所有交互基因，避免N+1查询
        val allInteractions = dataManager.useDatabase("saccharomyces")
            .from(GeneInteractionTable)
            .select(GeneInteractionTable.gene1, GeneInteractionTable.gene2)
            .where { GeneInteractionTable.gene1 inList genes or (GeneInteractionTable.gene2 inList genes) }
            .map {
                val gene1 = it[GeneInteractionTable.gene1].toString()
                val gene2 = it[GeneInteractionTable.gene2].toString()
                gene1 to gene2
            }

        // 按基因分组交互关系
        val interactionsByGene = mutableMapOf<String, MutableSet<String>>()
        allInteractions.forEach { (gene1, gene2) ->
            interactionsByGene.getOrPut(gene1) { mutableSetOf() }.add(gene2)
            interactionsByGene.getOrPut(gene2) { mutableSetOf() }.add(gene1)
        }

        // 批量查询表型数据，避免重复查询
        val allInteractionGenes = interactionsByGene.values.flatten().toSet()
        val genesWithPhenotype = if (allInteractionGenes.isNotEmpty()) {
            dataManager.useDatabase("saccharomyces")
                .from(GenePhenotypeTable)
                .select(GenePhenotypeTable.gene)
                .where {
                    (GenePhenotypeTable.phenotype like phe) and
                            (GenePhenotypeTable.gene inList allInteractionGenes.toList())
                }
                .map { it[GenePhenotypeTable.gene].toString() }
                .toSet()
        } else {
            emptySet()
        }

        val result = hashMapOf<String, List<String>>()
        val usedGenes = mutableSetOf<String>()

        genes.forEach { gene ->
            val interactions = interactionsByGene[gene] ?: emptySet()

            val filter = interactions
                .asSequence()
                .filter { it !in usedGenes && it !in genesWithPhenotype }
                .toList()

            usedGenes.addAll(filter)
            result[gene] = filter
        }

        // 批量转换符号，避免重复操作
        val filter = result.mapKeys { it.key.asSymbol("saccharomyces") }
            .mapValues { it.value.asSymbol("saccharomyces").values.toList() }

        println(gson.toJson(filter))
    }
}