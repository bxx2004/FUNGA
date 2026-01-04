package cn.revoist.lifephoton.module.funga.core.textmining.page

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBase
import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.core.textmining.model.TextMiningTypeBaseEntity
import cn.revoist.lifephoton.module.funga.core.textmining.table.TextMiningTypeTable
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.routing.RoutingCall
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select

/**
 * @author 6hisea
 * @date  2025/11/21 15:46
 * @description: None
 */
@RouteContainer("funga","text-mining-type")
object TextMiningTypeRoute : SqlBase(FungaPlugin, TextMiningTypeTable, TextMiningTypeBaseEntity::class.java,false){
    @Route(GET)
    suspend fun all(call: RoutingCall){
        call.ok(FungaPlugin.dataManager.useDatabase()
            .from(TextMiningTypeTable)
            .select(TextMiningTypeTable.name)
            .map {
                it.getString(1)!!
            })
    }
}