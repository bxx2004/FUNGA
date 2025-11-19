package cn.revoist.lifephoton.module.funga.core.textannotation.model

import cn.revoist.lifephoton.module.funga.data.table.type.Source
import cn.revoist.lifephoton.plugin.data.entity.Map

/**
 * @author 6hisea
 * @date  2025/11/10 18:02
 * @description: None
 */

class TaskMapper {
    @Map
    val id:Long = -1L
    @Map
    lateinit var source:Source
    @Map
    lateinit var text :String
    @Map
    lateinit var result:TextAnnotationResult
    @Map
    lateinit var taxonomy :String
    @Map
    val completion_user:Long = -1L
    @Map
    val completion_date:Long = -1L
    @Map
    val completion:Boolean = false
    @Map
    lateinit var  reason:String
}