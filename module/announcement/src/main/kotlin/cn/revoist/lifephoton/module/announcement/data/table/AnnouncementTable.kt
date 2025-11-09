package cn.revoist.lifephoton.module.announcement.data.table

import cn.revoist.lifephoton.plugin.anno.CreateTable
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/11/7 18:23
 * @description: None
 */
@CreateTable("announcement","""
    CREATE TABLE IF NOT EXISTS announcement (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR NOT NULL,
    create_time BIGINT NOT NULL,
    content VARCHAR NOT NULL,
    user_id BIGINT NOT NULL,
    priority INT NOT NULL
);
""")
object AnnouncementTable : Table<Nothing>("announcement") {
    val id = long("id").primaryKey()
    val title = varchar("title")
    val create_time = long("create_time")
    val content = varchar("content")
    val user_id = long("user_id")
    val priority = int("priority")
}