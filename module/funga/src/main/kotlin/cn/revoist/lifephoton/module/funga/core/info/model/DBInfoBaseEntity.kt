package cn.revoist.lifephoton.module.funga.core.info.model

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.BaseType
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.FILE
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.ID
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.STRING
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBaseEntity

/**
 * @author 6hisea
 * @date  2025/11/15 16:30
 * @description: None
 */
class DBInfoBaseEntity : SqlBaseEntity(){
    @BaseType(ID)
    val id = -1
    @BaseType(STRING)
    lateinit var name: String
    @BaseType(STRING)
    lateinit var taxonomy: String
    @BaseType(STRING)
    lateinit var description:String
    @BaseType(FILE)
    lateinit var image:String
}