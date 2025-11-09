package cn.revoist.lifephoton.system

import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.anno.AutoUse

/**
 * @author 6hisea
 * @date  2025/1/18 15:49
 * @description: None
 */
@AutoUse
object Lifephoton : Plugin() {
    override val name: String
        get() = "Lifephoton"
    override val author: String
        get() = "Haixu Liu"
    override val version: String
        get() = "beta-1"

    override fun load() {

    }
}