package cn.revoist.lifephoton.module.funga.core.common.model

import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.core.info.table.DBInfoTable
import cn.revoist.lifephoton.plugin.data.maps

/**
 * @author 6hisea
 * @date  2025/4/13 17:39
 * @description: None
 */
abstract class WithDatabasesRequest {
    private var databases:List<String> = emptyList()
    fun dbs():List<String>{
        val onlineDBs = FungaPlugin.dataManager.useDatabase()
            .maps(DBInfoTable,DBInfoTable.id)
            .map {
                it["name"] as String
            }
        if (databases.isEmpty()){
            return onlineDBs
        }
        if (databases.contains("all")){
            return onlineDBs
        }
        return databases.filter { onlineDBs.contains(it) }
    }
    fun setDatabases(dbs:List<String>){
        this.databases = dbs
    }
}
