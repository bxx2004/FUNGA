package cn.revoist.lifephoton.module.funga.core.textannotation.service

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.count
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.select
import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.core.textannotation.model.LogMapper
import cn.revoist.lifephoton.module.funga.core.textannotation.model.PointsMapper
import cn.revoist.lifephoton.module.funga.core.textannotation.model.TaskMapper
import cn.revoist.lifephoton.module.funga.core.textannotation.table.TextAnnotationDirtyTable
import cn.revoist.lifephoton.module.funga.core.textannotation.table.TextAnnotationLogTable
import cn.revoist.lifephoton.module.funga.core.textannotation.table.TextAnnotationPointsTable
import cn.revoist.lifephoton.plugin.computeDynamicInformation
import cn.revoist.lifephoton.plugin.data.bind
import cn.revoist.lifephoton.plugin.data.count
import cn.revoist.lifephoton.plugin.route.PagingPayloadResponse
import cn.revoist.lifephoton.tools.EmailSender
import kotlinx.coroutines.runBlocking
import org.ktorm.dsl.and
import org.ktorm.dsl.desc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.limit
import org.ktorm.dsl.map
import org.ktorm.dsl.plus
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import org.ktorm.support.postgresql.insertOrUpdate
import kotlin.math.ceil
import kotlin.random.Random

/**
 * @author 6hisea
 * @date  2025/11/10 15:20
 * @description: None
 */
object TextAnnotationService {

    fun getPoints(user: Long): PointsMapper? {
        return FungaPlugin.dataManager.useDatabase()
            .bind(TextAnnotationPointsTable, PointsMapper::class.java){
                select()
                    .where {
                        TextAnnotationPointsTable.id eq user
                    }
            }.firstOrNull()
    }

    fun withdrawPoints(user: Long,alipay: String) {
        val points = getPoints(user)!!
        if (points.points == 0.00){
            return
        }
        FungaPlugin.dataManager.useDatabase()
            .update(TextAnnotationPointsTable){
                set(TextAnnotationPointsTable.points,0.00)
                where {
                    TextAnnotationPointsTable.user_id eq user
                }
            }
        FungaPlugin.dataManager.useDatabase()
            .insert(TextAnnotationLogTable){
                set(it.user_id, user)
                set(it.task_id, -1)
                set(it.completion,true)
                set(it.points, -points.points)
                set(it.date,System.currentTimeMillis())
                set(it.reason,"Withdrawal by user using Alipay account: ${alipay}")
            }
        runBlocking {
            EmailSender.send(arrayListOf("l_haixu@163.com"),"用户提现申请","""
                用户ID：${user}
                提现金额：${points.points}
                支付宝账号：${alipay}
            """.trimIndent())
        }
    }

    fun getLogs(user: Long,pageSize:Int): PagingPayloadResponse<*> {
        return FungaPlugin.dataManager.useDynamicPagination(pageSize) { requestPage, size ->
            val count = FungaPlugin.dataManager.useDatabase().count(TextAnnotationLogTable){
                TextAnnotationLogTable.user_id eq user
            }
            val pages = ceil(count / size.toDouble()).toInt()
            return@useDynamicPagination computeDynamicInformation(requestPage, size, pages, FungaPlugin,TextAnnotationLogTable){
                TextAnnotationLogTable.user_id eq user
            }
        }
    }

    fun mark(user: Long,taskId: Long,result:Boolean,reason:String) {
        FungaPlugin.dataManager.useDatabase()
            .update(TextAnnotationDirtyTable){
                set(TextAnnotationDirtyTable.completion,result)
                set(TextAnnotationDirtyTable.completion_date, System.currentTimeMillis())
                set(TextAnnotationDirtyTable.reason, reason)
                where {
                    (TextAnnotationDirtyTable.id eq taskId) and  (TextAnnotationDirtyTable.completion_user eq user)
                }
            }
        val point = Random.nextDouble(0.01,0.05)
        FungaPlugin.dataManager.useDatabase()
            .insertOrUpdate(TextAnnotationPointsTable){
                set(it.user_id, user)
                set(it.points, point)
                onConflict(it.user_id) {
                    set(it.points, it.points + point)
                }
            }
        FungaPlugin.dataManager.useDatabase()
            .insert(TextAnnotationLogTable){
                set(it.user_id, user)
                set(it.task_id, taskId)
                set(it.completion,result)
                set(it.points, point)
                set(it.date,System.currentTimeMillis())
                set(it.reason,reason)
            }
    }

    fun getCurrentTaskId(user: Long): Long?{
        return FungaPlugin.dataManager.useDatabase()
            .from(TextAnnotationDirtyTable)
            .select(TextAnnotationDirtyTable.id)
            .where {
                (TextAnnotationDirtyTable.completion_user eq user) and (TextAnnotationDirtyTable.completion_date eq -1)
            }
            .limit(1)
            .map {
                it[TextAnnotationDirtyTable.id]?.toLong()
            }.firstOrNull()
    }

    fun getCurrentTask(user: Long): TaskMapper?{
        return FungaPlugin.dataManager.useDatabase().bind(TextAnnotationDirtyTable, TaskMapper::class.java){
            select()
                .where {
                    (TextAnnotationDirtyTable.completion_user eq user) and (TextAnnotationDirtyTable.completion_date eq -1)
                }
                .limit(1)
        }.firstOrNull()
    }

    fun getNextTaskId(): Long?{
        return FungaPlugin.dataManager.useDatabase()
            .from(TextAnnotationDirtyTable)
            .select(TextAnnotationDirtyTable.id)
            .where {
                (TextAnnotationDirtyTable.completion_user eq -1) and (TextAnnotationDirtyTable.completion_date eq -1)
            }
            .limit(1)
            .map {
                it[TextAnnotationDirtyTable.id]
            }.firstOrNull()
    }
    fun giveTask(user: Long):Boolean{
        val task = getNextTaskId()
        if (task != null) {
            FungaPlugin.dataManager.useDatabase()
                .update(TextAnnotationDirtyTable){
                    set(TextAnnotationDirtyTable.completion_user, user)
                    where {
                        TextAnnotationDirtyTable.id eq task
                    }
                }
            return true
        }else{
            return false
        }
    }
}