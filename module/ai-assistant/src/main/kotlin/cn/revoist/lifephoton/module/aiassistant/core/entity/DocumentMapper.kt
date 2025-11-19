package cn.revoist.lifephoton.module.aiassistant.core.entity

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.BOOLEAN
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.BaseType
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.DATE
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.NUMBER
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.STRING
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBaseEntity

/**
 * @author 6hisea
 * @date  2025/11/11 10:44
 * @description: None
 */
class DocumentMapper : SqlBaseEntity() {
    val id = -1L
    lateinit var doc_id:String
    @BaseType(DATE)
    val date = -1L
    @BaseType(NUMBER)
    val uploader = -1L
    @BaseType(STRING)
    lateinit var citation : String
    @BaseType(STRING)
    lateinit var source : String

    @BaseType(BOOLEAN)
    var review:Boolean = false
}