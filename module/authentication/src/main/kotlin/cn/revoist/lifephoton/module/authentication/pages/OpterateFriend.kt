package cn.revoist.lifephoton.module.authentication.pages

import cn.revoist.lifephoton.module.authentication.Auth
import cn.revoist.lifephoton.module.authentication.asEntity
import cn.revoist.lifephoton.module.authentication.data.table.FriendTable
import cn.revoist.lifephoton.module.authentication.data.table.hasFriend
import cn.revoist.lifephoton.module.authentication.getUser
import cn.revoist.lifephoton.module.authentication.isLogin
import cn.revoist.lifephoton.plugin.match
import cn.revoist.lifephoton.plugin.route.Api
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.error
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.routing.RoutingCall
import org.ktorm.dsl.and
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where

/**
 * @author 6hisea
 * @date  2025/8/24 16:02
 * @description: None
 */
@RouteContainer("auth","friend")
object OpterateFriend {
    @Route(GET)
    @Api("添加朋友")
    suspend fun add(call:RoutingCall) {
        val id = call.parameters["id"]
        try {
            id!!.toLong()
        }catch (e: Exception){
            call.error("id is wrong")
            return
        }
        if (id.isEmpty()) {
            call.error("id don't exist")
        }
        call.match {
            isLogin()
        }.then {
            val user = getUser().asEntity!!
            if (user.hasFriend(id.toLong())){
                error("already has friend")
            }else{
                Auth.dataManager.useDatabase()
                    .insert(FriendTable){
                        set(FriendTable.from,user.id)
                        set(FriendTable.to,id!!.toLong())
                    }
            }
            ok("success")
        }.default {
            error("Not login")
        }
    }
    @Route(GET)
    @Api("删除朋友")
    suspend fun delete(call:RoutingCall) {
        val id = call.parameters["id"]
        if (id != null) {
            call.error("id don't exist")
        }
        call.match {
            isLogin()
        }.then {
            val user = getUser().asEntity!!
            Auth.dataManager.useDatabase()
                .delete(FriendTable){
                    (FriendTable.from eq user.id) and (FriendTable.to eq id!!.toLong())
                }
            ok("success")
        }.default {
            error("Not login")
        }
    }
    @Route(GET)
    @Api("获取朋友")
    suspend fun get(call:RoutingCall) {
        call.match {
            isLogin()
        }.then {
            val user = getUser().asEntity!!
            ok(Auth.dataManager.useDatabase()
                .from(FriendTable)
                .select(FriendTable.to)
                .where {
                    FriendTable.from eq user.id
                }.map {
                    it.get(FriendTable.to)
                })
        }.default {
            error("Not login")
        }
    }
}