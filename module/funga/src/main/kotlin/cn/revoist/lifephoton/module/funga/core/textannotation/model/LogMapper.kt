package cn.revoist.lifephoton.module.funga.core.textannotation.model

import cn.revoist.lifephoton.plugin.data.entity.Map

/**
 * @author 6hisea
 * @date  2025/11/10 18:21
 * @description: None
 */
class LogMapper {
    @Map
    val id : Long = -1L
    @Map
    val user_id : Long = -1L
    @Map
    val task_id : Long = -1L
    @Map
    val completion : Boolean = false
    @Map
    val points : Double = 0.0
    @Map
    val date : Long = -1L
    @Map
    lateinit var  reason:String
}