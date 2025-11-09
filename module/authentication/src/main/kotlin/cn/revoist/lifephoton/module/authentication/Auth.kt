package cn.revoist.lifephoton.module.authentication

import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.anno.AutoUse
import cn.revoist.lifephoton.plugin.event.events.AuthenticationEvent
import cn.revoist.lifephoton.plugin.event.registerListener

/**
 * @author 6hisea
 * @date  2025/1/7 19:54
 * @description: Auth
 */
@AutoUse
object Auth :Plugin(){
    override val name: String
        get() = "Auth"
    override val author: String
        get() = "Haixu Liu"
    override val version: String
        get() = "1"

    override fun load() {
        registerListener(AuthenticationEvent::class.java){
            if (cn.revoist.lifephoton.module.authentication.data.Tools.checkToken(it.user)){
                it.truth = true
            }
        }
    }
}