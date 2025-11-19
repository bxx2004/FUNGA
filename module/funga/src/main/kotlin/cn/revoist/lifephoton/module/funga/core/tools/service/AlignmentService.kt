package cn.revoist.lifephoton.module.funga.core.tools.service

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.select
import cn.revoist.lifephoton.module.filemanagement.FileManagementAPI
import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.core.tools.table.AlignmentTable
import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.data.sqltype.gson
import cn.revoist.lifephoton.tools.submit
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.limit
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import java.io.File
import kotlin.random.Random

/**
 * @author 6hisea
 * @date  2025/11/15 16:15
 * @description: None
 */
object AlignmentService {
    val fileManager = FileManagementAPI.createStaticFileManager(FungaPlugin,"alignment")
    fun alignment(file: File,db:String,type: String,eValue: Double = 0.001):String{
        fun getCommand(target: File) : String {
            val symbol = if (type == "dna"){
                "blastx"
            }else{
                "blastp"
            }
            return if (FungaPlugin.getOS() == Plugin.OS.WINDOWS){
                "\"${FungaPlugin.diamondExec}\" ${symbol} --db \"${FungaPlugin.dmnd(db)}\" --query \"${file.absolutePath}\" --out \"${target.absolutePath}\"  --max-target-seqs 1 --evalue $eValue --threads 64"
            }else{
                "${FungaPlugin.diamondExec} ${symbol} --db ${FungaPlugin.dmnd(db)} --query ${file.absolutePath} --out ${target.absolutePath}  --max-target-seqs 1 --evalue $eValue --threads 64"
            }
        }

        val id = (System.currentTimeMillis() + Random(1000).nextInt()).toString()
        submit(-1,-1){
            try {
                fileManager.putStaticFileWithTemp(id){
                    val command = getCommand(it)
                    val process = Runtime.getRuntime().exec(command)
                    process.waitFor()
                    val result = arrayListOf<HashMap<String,Any>>()
                    it.readLines().forEach {
                        val line = it.split("\t")
                        result.add(hashMapOf(
                            "qseqid" to line[0],
                            "sseqid" to line[1],
                            "pident" to line[2].toDouble(),
                            "length" to line[3].toInt(),
                            "mismatch" to line[4].toInt(),
                            "gapopen" to line[5].toInt(),
                            "qstart" to line[6].toInt(),
                            "qend" to line[7].toInt(),
                            "sstart" to line[8].toInt(),
                            "send" to line[9].toInt(),
                            "evalue" to line[10].toDouble(),
                            "bitscore" to line[11].toDouble(),
                        ))
                    }
                    FungaPlugin.dataManager.useDatabase()
                        .insert(AlignmentTable){
                            set(it.type,"diamond")
                            set(it.date,System.currentTimeMillis())
                            set(it.analysis_id,id)
                            set(it.species,db)
                            set(it.result, gson.toJson(result))
                        }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        return id
    }
    fun isReady(id:String):Boolean{
        return FungaPlugin.dataManager.useDatabase()
            .from(AlignmentTable)
            .select(AlignmentTable.analysis_id)
            .where {
                AlignmentTable.analysis_id eq id
            }
            .limit(1)
            .map {
                1
            }.count() != 0
    }
    fun getAlignment(id:String): List<Map<String,Any>>{
        return FungaPlugin.dataManager.useDatabase()
            .from(AlignmentTable)
            .select(AlignmentTable.result)
            .where {
                AlignmentTable.analysis_id eq id
            }
            .limit(1)
            .map {
                gson.fromJson(it.get(AlignmentTable.result).toString(),Any::class.java)
            }.first() as List<Map<String,Any>>
    }
}