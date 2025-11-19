package cn.revoist.lifephoton.module.homepage.data.entity

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.BaseType
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.*
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBaseEntity

/**
 * @author 6hisea
 * @date  2025/11/10 11:17
 * @description: None
 */
class BarEntity : SqlBaseEntity(){
    @BaseType(ID)
    val id = -1
    @BaseType(FILE)
    lateinit var image:String
    lateinit var url:String
}