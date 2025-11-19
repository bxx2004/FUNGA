package cn.revoist.lifephoton.module.authentication.data.table

import cn.revoist.lifephoton.module.authentication.data.entity.UserDataEntity
import cn.revoist.lifephoton.plugin.anno.CreateTable
import cn.revoist.lifephoton.plugin.data.sqltype.obj
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/1/8 11:56
 * @description: None
 */
@CreateTable("auth", dbName = "auth", value = """
CREATE TABLE if not exists user_data (
    username VARCHAR NOT NULL,
    password VARCHAR NOT NULL,
    email VARCHAR NOT NULL,
    id SERIAL PRIMARY KEY,
    "group" VARCHAR NOT NULL,
    permissions JSONB NOT NULL,
    access_token VARCHAR NOT NULL,
    refresh_token VARCHAR NOT NULL,
    data JSONB NOT NULL,
    avatar VARCHAR NOT NULL
);
""")
object UserDataTable :Table<UserDataEntity>("user_data") {
    var username = varchar("username").bindTo { it.username }
    var password = varchar("password").bindTo { it.password }
    var email = varchar("email").bindTo { it.email }
    var id = long("id").bindTo { it.id }
    var group = varchar("group").bindTo { it.group }
    var permissions = obj<List<String>>("permissions").bindTo { it.permissions }
    var accessToken = varchar("access_token").bindTo { it.accessToken }
    var refreshToken = varchar("refresh_token").bindTo { it.refreshToken }
    var data = obj<HashMap<String,Any>>("data").bindTo { it.data }
    var avatar = varchar("avatar").bindTo { it.avatar }
}