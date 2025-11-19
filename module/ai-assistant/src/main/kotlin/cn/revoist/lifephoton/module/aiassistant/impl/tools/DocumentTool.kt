package cn.revoist.lifephoton.module.aiassistant.impl.tools

import cn.revoist.lifephoton.module.aiassistant.core.service.DocumentService
import cn.revoist.lifephoton.plugin.data.sqltype.gson

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import dev.langchain4j.invocation.InvocationParameters

/**
 * @author 6hisea
 * @date  2025/11/13 16:18
 * @description: None
 */
object DocumentTool {
    data class ResultMapper(val documents: List<Documents>, val count:Int){

    }
    data class Documents(val citation: String,val source: String)
    @Tool(name = "Document Review", value = ["Get the documents and papers in the document library based on the information"])
    fun GET_DOCUMENT(@P("keywords") query: String, parameters: InvocationParameters) : String {
        return try {
            val result = DocumentService.search(parameters.get("user_id"),query,20)
            val payload = result.payload as List<Map<String,Any>>
            val res =  ResultMapper(payload.map {
                Documents(it["citation"].toString(),it["source"].toString())
            },if (payload.isEmpty()) 0 else if (payload.size < 20) payload.size else result.allPages * 20)
            if (res.count == 0) return "empty."
            var str = ""

            if (res.documents.isNotEmpty()){
                str += "| Citation & Title | Source |\n" +
                        "|------|------|\n"
            }

            res.documents.forEach {
                val so = gson.fromJson(it.source,Map::class.java)
                str += "| ${it.citation} | [${so["name"]}](${so["link"]}) |\n"
            }
            str += if (res.count == 0){
                "Empty."
            }else{
                "Total: ${res.count}"
            }
            str
        }catch (e: Exception){
            "empty."
        }
    }
}