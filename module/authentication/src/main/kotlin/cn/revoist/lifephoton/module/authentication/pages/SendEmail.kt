package cn.revoist.lifephoton.module.authentication.pages

import cn.revoist.lifephoton.plugin.anno.AutoRegister
import cn.revoist.lifephoton.plugin.route.RoutePage
import cn.revoist.lifephoton.plugin.route.error
import cn.revoist.lifephoton.plugin.route.message
import cn.revoist.lifephoton.tools.submit
import io.ktor.http.*
import io.ktor.server.routing.*
import net.axay.simplekotlinmail.delivery.send
import net.axay.simplekotlinmail.email.emailBuilder

/**
 * @author 6hisea
 * @date  2025/1/8 19:58
 * @description: None
 */
@AutoRegister("auth")
object SendEmail : RoutePage("send-email",false,false) {
    override fun methods(): List<HttpMethod> {
        return listOf(HttpMethod.Get)
    }

    override suspend fun onGet(call: RoutingCall) {
        val email = call.queryParameters["email"]
        if (email.isNullOrEmpty()) {
            call.error("Email is not empty")
            return
        }
        if (Register.emailCodeCache.containsKey(email)) {
            call.error("code is not expired")
            return
        }
        val code = cn.revoist.lifephoton.module.authentication.data.Tools.generateCode()
        Register.emailCodeCache[email] = code
        submit(1000*60*5,-1){
            Register.emailCodeCache.remove(email)
            it.cancel()
        }
        //发送邮件
        emailBuilder {
            from("长春市锐沃科技有限公司","no-replay-revoist@qq.com")
            to(email)

            withSubject("Revo(锐沃) 注册行为验证")
            withPlainText("【锐沃科技】您好，您的账号正在进行注册操作，切勿将验证码泄露于他人，5分钟内有效。验证码：${code} 。")
        }.send()
        call.message("successfuly")
    }
}