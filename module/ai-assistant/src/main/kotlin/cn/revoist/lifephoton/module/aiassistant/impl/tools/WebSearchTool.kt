package cn.revoist.lifephoton.module.aiassistant.impl.tools

import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import dev.langchain4j.web.search.WebSearchEngine
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine


data class WebToolResult(val title: String,val url:String,val snippet: String,val content:String?){
}
object WebSearchTool {
    val webSearchEngine = TavilyWebSearchEngine.builder()
        .apiKey(AIAssistant.getConfig("llm.tavilyKey","xxxxxxx"))
        .build()
    @Tool(name = "Search Online", value = ["This tool can be used to perform web searches using search engines such as Google, particularly when seeking information about recent events."])
    fun searchOnline(@P("Web search query") query: String): List<WebToolResult> {
        val result = webSearchEngine.search(query).results()
        if (result.isEmpty()) {
            return emptyList()
        }
        return webSearchEngine.search(query).results().map {
            WebToolResult(it.title(),it.url().toString(),it.snippet(),it.content())
        }
    }
}