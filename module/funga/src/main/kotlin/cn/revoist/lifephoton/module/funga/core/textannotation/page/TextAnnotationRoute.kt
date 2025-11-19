package cn.revoist.lifephoton.module.funga.core.textannotation.page

import cn.revoist.lifephoton.module.authentication.validateLogin
import cn.revoist.lifephoton.module.funga.core.textannotation.service.TextAnnotationService
import cn.revoist.lifephoton.plugin.pageSize
import cn.revoist.lifephoton.plugin.route.Api
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.checkParameters
import cn.revoist.lifephoton.plugin.route.error
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.routing.RoutingCall

/**
 * @author 6hisea
 * @date  2025/11/10 15:20
 * @description: None
 */
@RouteContainer("funga","text-annotation")
object TextAnnotationRoute {
    @Route(GET)
    @Api("申请一个文本标注任务")
    suspend fun apply(call: RoutingCall){
        call.validateLogin("text-annotation.*") { user ->
            val current = TextAnnotationService.getCurrentTaskId(user.id)
            if (current == null){
                if (TextAnnotationService.giveTask(user.id)){
                    ok("success")
                }else{
                    error("There are no tasks at the moment, so please be patient")
                }
            }else{
                error("You have an unfinished task: $current")
            }
        }
    }
    @Route(GET)
    @Api("获取当前任务数据")
    suspend fun get(call: RoutingCall){
        call.validateLogin("text-annotation.*") { user ->
            val current = TextAnnotationService.getCurrentTask(user.id)
            if (current == null){
                error("You don't have a task")
            }else{
                ok(current)
            }
        }
    }
    @Route(GET)
    @Api("完成一个任务的标记标出")
    suspend fun mark(call: RoutingCall){
        call.validateLogin("text-annotation.*") { user ->
            checkParameters("result","reason")
            val result = queryParameters["result"]!!
            val reason = queryParameters["reason"]!!
            val current = TextAnnotationService.getCurrentTaskId(user.id)
            if (current == null){
                error("You don't have task")
            }else{
                TextAnnotationService.mark(user.id,current,result.toBoolean(),reason)
                ok("success")
            }
        }
    }
    @Route(GET)
    @Api("获取积分余额")
    suspend fun point(call: RoutingCall){
        call.validateLogin("text-annotation.*") { user ->
            ok(TextAnnotationService.getPoints(user.id)?.points?:0.00)
        }
    }
    @Route(GET)
    @Api("获取日志")
    suspend fun logs(call: RoutingCall){
        call.validateLogin("text-annotation.*") { user ->
            ok(TextAnnotationService.getLogs(user.id,pageSize()))
        }
    }
    @Route(GET)
    @Api("提现申请")
    suspend fun withdraw(call: RoutingCall){
        call.validateLogin("text-annotation.*") { user ->
            checkParameters("alipay")
            val alipay = queryParameters["alipay"]!!
            TextAnnotationService.withdrawPoints(user.id,alipay)
            ok(true,"your application is successful, it will be processed for you within 7 working days.")
        }
    }
}