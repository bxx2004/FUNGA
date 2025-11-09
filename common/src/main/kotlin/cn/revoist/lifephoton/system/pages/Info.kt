package cn.revoist.lifephoton.system.pages

import cn.revoist.lifephoton.Booster
import cn.revoist.lifephoton.plugin.data.json.jsonObject
import cn.revoist.lifephoton.plugin.getPlugins
import cn.revoist.lifephoton.plugin.route.*
import cn.revoist.lifephoton.plugin.route.Route
import io.ktor.server.routing.*
/**
 * @author 6hisea
 * @date  2025/1/18 15:51
 * @description: None
 */
@RouteContainer("lifephoton","info")
object Info{
    @Route(GET)
    @Api("获取系统的元信息")
    suspend fun getMeta(call: RoutingCall){
         call.ok(jsonObject {
             put("version",Booster.VERSION)
             array("plugins"){
                 for (plugin in getPlugins()) {
                     add(
                         jsonObject {
                             put("id", plugin.id)
                             put("name", plugin.name)
                             put("version", plugin.version)
                             put("author", plugin.author)
                         }
                     )
                 }
             }
         })
    }
}