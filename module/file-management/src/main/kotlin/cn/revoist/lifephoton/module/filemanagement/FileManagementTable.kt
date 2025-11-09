package cn.revoist.lifephoton.module.filemanagement

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/7/28 00:29
 * @description: None
 */
object FileManagementTable:Table<FileManagementTable.FileUnit>("file_management") {
    val id = int("id").primaryKey().bindTo { it.id }
    val file_id = varchar("file_id").bindTo { it.file_id }
    val user_id = long("user_id").bindTo { it.user_id }
    val path = varchar("path").bindTo { it.path }
    val timestamp = long("timestamp").bindTo { it.timestamp }
    val name = varchar("name").bindTo { it.name }
    val upload = boolean("upload").bindTo { it.upload }
    val source = varchar("source").bindTo { it.source }
    interface FileUnit:Entity<FileUnit>{
        companion object : Entity.Factory<FileUnit>()
        val id:Int
        var file_id:String
        var user_id: Long
        var path:String
        var timestamp:Long
        var name:String
        var source:String
        var upload: Boolean
    }
}