package cn.revoist.lifephoton.module.funga

import cn.revoist.lifephoton.module.funga.data.database.vector.MilvusDatabase
import cn.revoist.lifephoton.module.funga.core.info.table.DBInfoTable
import cn.revoist.lifephoton.module.funga.data.table.GeneTable
import cn.revoist.lifephoton.module.funga.tools.fungaIdMapCache
import cn.revoist.lifephoton.module.funga.tools.symbolMapCache
import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.anno.AutoUse
import cn.revoist.lifephoton.plugin.data.maps
import cn.revoist.lifephoton.plugin.data.mapsWithColumn
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
        //try load mapping
        println("Pre loading mapping")
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
}