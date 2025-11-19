package cn.revoist.lifephoton.module.homepage

import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.anno.AutoUse

/**
 * @author 6hisea
 * @date  2025/1/10 11:08
 * @description: None
 */
@AutoUse
object Homepage : Plugin(){
    override val name: String
        get() = "Homepage"
    override val author: String
        get() = "Haixu Liu"
    override val version: String
        get() = "beta-1"


    override fun load() {

    }
}