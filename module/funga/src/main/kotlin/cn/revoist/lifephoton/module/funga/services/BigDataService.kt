package cn.revoist.lifephoton.module.funga.services

import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.ai.assistant.DocumentAssistant
import cn.revoist.lifephoton.module.funga.data.entity.ai.GeneResult
import cn.revoist.lifephoton.module.funga.data.entity.inneral.DynamicDataResult
import cn.revoist.lifephoton.module.funga.data.table.BigdataWallet
import cn.revoist.lifephoton.module.funga.data.table.BigdataWalletLog
import cn.revoist.lifephoton.module.funga.data.table.DynamicCacheTable
import cn.revoist.lifephoton.module.funga.data.table.DynamicDataTable
import cn.revoist.lifephoton.module.funga.data.table.type.Source
import cn.revoist.lifephoton.plugin.data.first
import cn.revoist.lifephoton.plugin.data.maps
import cn.revoist.lifephoton.plugin.data.sqltype.gson
import cn.revoist.lifephoton.plugin.route.Api
import com.google.gson.reflect.TypeToken
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.data.document.splitter.DocumentSplitters
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.count
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Column
import java.io.File
import kotlin.random.Random


/**
 * @author 6hisea
 * @date  2025/8/27 14:16
 * @description: None
 */
object BigDataService {
    var type = object : TypeToken<List<DynamicDataResult>>(){}.type

    fun next(userId: Long):HashMap<String,Any?>{
        val data = FungaPlugin.dataManager.useDatabase()
            .maps(DynamicDataTable){
                where {
                    DynamicDataTable.user_id eq userId
                }
            }.firstOrNull()
        if (data != null) return data
        val fir =  FungaPlugin.dataManager.useDatabase().first(DynamicDataTable){
            where {
                DynamicDataTable.user_id eq -1
            }
        }
        val a = fir.get("id").toString().toLong()
        val b = fir.get("user_id").toString().toLong()
        FungaPlugin.dataManager.useDatabase().update(DynamicDataTable){
            set(DynamicDataTable.user_id,b)
            where {
                DynamicDataTable.id eq a
            }
        }
        return fir
    }
    fun hasNext():Boolean{
        return FungaPlugin.dataManager.useDatabase().sequenceOf(DynamicDataTable).count() > 0
    }

    fun mark(id:Long,state: Boolean,userID: Long): Double{
        if (!state){
            FungaPlugin.dataManager.useDatabase()
                .delete(DynamicDataTable){
                    DynamicDataTable.id eq id
                }
            if (Random.nextInt(1,100) < 5){
                return Random.nextDouble(0.01,0.15)
            }
            return 0.01
        }
        FungaPlugin.dataManager.useDatabase()
            .from(DynamicDataTable)
            .select()
            .where { DynamicDataTable.id eq id }
            .forEach { res->
                FungaPlugin.dataManager.useDatabase().insert(DynamicCacheTable){
                    set(DynamicCacheTable.id, res.get(DynamicDataTable.id))
                    set(DynamicCacheTable.result,res.get(DynamicDataTable.result))
                    set(DynamicCacheTable.date, System.currentTimeMillis())
                    set(DynamicCacheTable.source,res.get(DynamicDataTable.source))
                    set(DynamicCacheTable.content,res.get(DynamicDataTable.content))
                    set(DynamicCacheTable.user_id,userID)
                }
            }
        FungaPlugin.dataManager.useDatabase()
            .delete(DynamicDataTable){
                DynamicDataTable.id eq id
            }
        return 0.0
    }
    fun wallet(userID:Long): Double{
        val exist =FungaPlugin.dataManager.useDatabase().from(BigdataWallet)
            .select()
            .where { BigdataWallet.user_id eq userID }
            .asIterable().iterator().hasNext()
        if (!exist) {
            FungaPlugin.dataManager.useDatabase().insert(BigdataWallet){
                set(BigdataWallet.user_id, userID)
                set(BigdataWallet.point,0.00)
            }
        }
        return FungaPlugin.dataManager.useDatabase().first(BigdataWallet, BigdataWallet.user_id){
            where {
                BigdataWallet.user_id eq userID
            }
        }.get("point").toString().toDouble()
    }
    fun walletLog(userID:Long): List<HashMap<String,Any?>>{
        return FungaPlugin.dataManager.useDatabase().last{
            where {
                BigdataWalletLog.user_id eq userID
            }
        }
    }
    fun updateWallet(userID:Long, wallet:Double){
        val exist =FungaPlugin.dataManager.useDatabase().from(BigdataWallet)
            .select()
            .where { BigdataWallet.user_id eq userID }
            .asIterable().iterator().hasNext()
        if (!exist) {
            FungaPlugin.dataManager.useDatabase().insert(BigdataWallet){
                set(BigdataWallet.user_id, userID)
                set(BigdataWallet.point,0.00)
            }
        }
        val yue = FungaPlugin.dataManager.useDatabase().from(BigdataWallet)
            .select()
            .where { BigdataWallet.user_id eq userID }
            .asIterable().first().get(BigdataWallet.point)
        FungaPlugin.dataManager.useDatabase().update(BigdataWallet){
            set(BigdataWallet.point,yue!! + wallet)
            where {
                BigdataWallet.user_id eq userID
            }
        }
        FungaPlugin.dataManager.useDatabase().insert(BigdataWalletLog){
            set(BigdataWalletLog.user_id,userID)
            set(BigdataWalletLog.point,yue)
            set(BigdataWalletLog.method,wallet)
            set(BigdataWalletLog.note,"rewards")
        }
    }
    private fun Database.last(vararg columns: Column<*>, func: Query.()-> Query? = {null}): List<HashMap<String,Any?>>{
        val selected = ArrayList<Column<*>>()
        BigdataWalletLog.columns.forEach {
            if (!columns.map { it.name }.contains(it.name)){
                selected.add(it)
            }
        }
        val re = ArrayList<HashMap<String,Any?>>()
        var a = from(BigdataWalletLog).select()
            .limit(10).orderBy(BigdataWalletLog.id.desc())
        a = func(a)?:a
        a.forEach { row->
            val r = HashMap<String,Any?>()
            selected.forEach {
                r[it.name] = row[it]
            }
            re.add(r)
        }
        return re
    }
}