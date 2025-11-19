package cn.revoist.lifephoton.module.filemanagement

import cn.revoist.lifephoton.module.authentication.data.Tools
import cn.revoist.lifephoton.module.authentication.data.entity.UserDataEntity
import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.PluginAPI
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.html.insert
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import java.io.File

/**
 * @author 6hisea
 * @date  2025/1/22 16:57
 * @description: None
 */
object FileManagementAPI : PluginAPI{
    override val plugin: Plugin
        get() = FileManagement
    @Deprecated("已过时")
    fun createStaticFileManager(plugin: Plugin):StaticFileManager{
        return StaticFileManager(plugin.id)
    }
    fun createStaticFileManager(plugin: Plugin,child: String):StaticFileManager{
        return StaticFileManager(plugin.id+"/$child")
    }
    fun writePublic(name:String, user: UserDataEntity?, upload: Boolean, func:(File)->Unit):String{
        val code = System.currentTimeMillis().toString() + "-" + Tools.generateCode()
        val f = File(FileManagement.workdir, "$code.lpa")
        if (f.exists()){
            f.delete()
        }
        f.createNewFile()
        func(f)
        FileManagement.dataManager.useDefaultDatabase()
            .insert(FileManagementTable){
                set(FileManagementTable.name,name)
                set(FileManagementTable.source,"public")
                set(FileManagementTable.path,f.absolutePath)
                set(FileManagementTable.user_id,user?.id?:-1)
                set(FileManagementTable.timestamp,System.currentTimeMillis())
                set(FileManagementTable.file_id,code)
                set(FileManagementTable.upload,upload)
            }
        return code
    }
    fun write(name:String, user: UserDataEntity?, upload: Boolean, source: Plugin, func:(File)->Unit):String{
        val code = System.currentTimeMillis().toString() + "-" + Tools.generateCode()
        val f = File(FileManagement.workdir, "$code.lpa")
        if (f.exists()){
            f.delete()
        }
        f.createNewFile()
        func(f)
        FileManagement.dataManager.useDefaultDatabase()
            .insert(FileManagementTable){
                set(FileManagementTable.name,name)
                set(FileManagementTable.source,source.id)
                set(FileManagementTable.path,f.absolutePath)
                set(FileManagementTable.user_id,user?.id?:-1)
                set(FileManagementTable.timestamp,System.currentTimeMillis())
                set(FileManagementTable.file_id,code)
                set(FileManagementTable.upload,upload)
        }
        return code
    }

    fun findRecordById(id:String): FileManagementTable.FileUnit?{
        return FileManagement.dataManager.useDefaultDatabase().sequenceOf(FileManagementTable).find {
            it.file_id eq id
        }
    }
    fun findRecordByUser(id: Long): List<FileManagementTable.FileUnit>{
        return FileManagement.dataManager.useDefaultDatabase().sequenceOf(FileManagementTable).filter {
            it.user_id eq id
        }.toList()
    }

    fun findFileById(id: String): File?{
        val filePath = FileManagement.dataManager.useDefaultDatabase().from(FileManagementTable)
            .select(FileManagementTable.path)
            .where {
                FileManagementTable.file_id eq id
            }.map {
                it[FileManagementTable.path].toString()
            }.firstOrNull()
        if (filePath != null){
            //path null
            return File(filePath)
        }
        return null
    }
    fun findFileByUser(id: Long): List<File>{
        return FileManagement.dataManager.useDefaultDatabase().from(FileManagementTable)
            .select(FileManagementTable.path)
            .where {
                FileManagementTable.user_id eq id
            }.map {
                File(it.get(FileManagementTable.file_id).toString())
            }
    }

    class StaticFileManager(private val uniqueId:String){
        fun putStaticFileWithTemp(p:String,delete:Boolean = false,func:(file:File)->Unit){
            val file = File(FileManagement.staticDir,"$uniqueId/$p.tmp")
            if (!file.parentFile.exists()){
                file.parentFile.mkdirs()
            }
            if (!file.exists()){
                file.createNewFile()
            }
            func(file)
            file.renameTo(File(file.absolutePath.replace(".tmp","")))
            if (delete){
                file.delete()
            }
        }
        @Deprecated("Unsafe")
        fun putStaticFile(p:String,func:(file:File)->Unit){
            val file = File(FileManagement.staticDir,"$uniqueId/$p")
            if (!file.parentFile.exists()){
                file.parentFile.mkdirs()
            }
            if (!file.exists()){
                file.createNewFile()
            }
            func(file)
        }
        fun identityStaticFile(p:String,func:(file:File)->Unit){
            val file = File(FileManagement.staticDir,"$uniqueId/$p")
            if (!file.parentFile.exists()){
                file.parentFile.mkdirs()
            }
            func(file)
        }
        fun getStaticFile(p:String):File{
            return File(FileManagement.staticDir,"$uniqueId/$p")
        }
    }
}