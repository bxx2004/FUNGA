package cn.revoist.lifephoton.module.announcement.data.entity

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.BaseType
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.DATE
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.MARKDOWN
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBaseEntity

/**
 * @author 6hisea
 * @date  2025/11/7 18:36
 * @description: None
 */
class AnnouncementEntity : SqlBaseEntity(){
    val id = -1L
    lateinit var title:String
    @BaseType(DATE)
    val create_time = -1L
    @BaseType(MARKDOWN)
    lateinit var content:String
    val user_id = -1L
    val priority = 0L
}