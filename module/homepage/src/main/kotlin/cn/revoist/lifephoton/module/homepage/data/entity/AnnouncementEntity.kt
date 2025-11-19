package cn.revoist.lifephoton.module.homepage.data.entity

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.BaseType
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.DATE
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.ID
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.MARKDOWN
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.NUMBER
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBaseEntity

/**
 * @author 6hisea
 * @date  2025/11/7 18:36
 * @description: None
 */
class AnnouncementEntity : SqlBaseEntity(){
    @BaseType(ID)
    val id = -1L
    lateinit var title:String
    @BaseType(DATE)
    val create_time = -1L
    @BaseType(MARKDOWN)
    lateinit var content:String
    val user_id = -1L
    @BaseType(NUMBER, min = 0, max = 2)
    val priority = 0L
}