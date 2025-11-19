package cn.revoist.lifephoton.tools

import net.axay.simplekotlinmail.delivery.send
import net.axay.simplekotlinmail.email.emailBuilder
import java.io.File

/**
 * @author 6hisea
 * @date  2025/11/10 21:12
 * @description: None
 */
object EmailSender {
    private val templates = HashMap<String, String>()
    fun registerTemplate(key: String, value: String) {
        templates[key] = value
    }
    suspend fun send(target:List<String>, title: String, content:String){
        target.forEach {
            emailBuilder {
                from("长春市锐沃科技有限公司","no-replay-revoist@qq.com")
                to(it)
                withSubject(title)
                withPlainText(content)
            }.send()
        }
    }
    suspend fun send(target:List<String>, title: String, file: File,vars:Map<String,String>){
        target.forEach {
            emailBuilder {
                from("长春市锐沃科技有限公司","no-replay-revoist@qq.com")
                to(it)
                withSubject(title)
                var text  = file.readText()
                vars.forEach {
                    text.replace("{{${it.key}}}", it.value)
                }
                withHTMLText(text)
            }.send()
        }
    }
    suspend fun send(target:List<String>, title: String, template:String,vars:Map<String,String>){
        target.forEach {
            emailBuilder {
                from("长春市锐沃科技有限公司","no-replay-revoist@qq.com")
                to(it)
                withSubject(title)
                var text  = templates[template]!!
                vars.forEach {
                    text.replace("{{${it.key}}}", it.value)
                }
                withHTMLText(text)
            }.send()
        }
    }
}