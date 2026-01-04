package cn.revoist.lifephoton.module.fungipattern

import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.anno.AutoUse

/**
 * @author 6hisea
 * @date  2025/12/9 20:45
 * @description: None
 */
@AutoUse
object FungiPattern : Plugin(){
    override val name: String
        get() = "fungi-pattern"
    override val author: String
        get() = "bxx2004"
    override val version: String
        get() = "beta-1"

    override fun load() {
        """
            入库检索
            向量+SIFT降序重排校验 and 输出
        """.trimIndent()
    }
}