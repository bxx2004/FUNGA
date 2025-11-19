package cn.revoist.lifephoton.module.aiassistant.impl.rag


import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import cn.revoist.lifephoton.module.aiassistant.AIAssistantAPI
import cn.revoist.lifephoton.module.aiassistant.core.entity.ChatOption
import cn.revoist.lifephoton.module.aiassistant.core.service.RoleServices
import cn.revoist.lifephoton.module.aiassistant.impl.db.computeTableIndex
import cn.revoist.lifephoton.module.aiassistant.impl.memory.PostgreSQLMemoryStore
import cn.revoist.lifephoton.module.aiassistant.impl.tools.DocumentTool
import cn.revoist.lifephoton.module.authentication.data.table.whoShareMe
import cn.revoist.lifephoton.tools.submit
import dev.langchain4j.invocation.InvocationParameters
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.rag.DefaultRetrievalAugmentor
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.query.router.DefaultQueryRouter
import dev.langchain4j.service.AiServices
import cn.revoist.lifephoton.module.aiassistant.impl.tools.WebSearchTool
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors


/**
 * @author 6hisea
 * @date  2025/11/12 17:34
 * @description: None
 */
object KnowledgeBaseAggregator {
    private val contentInjector = FixContentInjector.builder()
            .metadataKeysToInclude(listOf("citation","uploader","doc_id"))
            .build()
    private val executor = Executors.newCachedThreadPool()

    private val cache = ConcurrentHashMap<Long, Any>()
    private val friendsCache = ConcurrentHashMap<Long, List<Long>>()
    private val updateFriends = CopyOnWriteArrayList<Long>()
    private val optionCache = ConcurrentHashMap<Long, ChatOption>()

    fun startListener(){
        submit(1,1000*60*10) {
            friendsCache.keys.forEach {
                val newFriends = uploaders(it)
                if (newFriends != friendsCache){
                    friendsCache[it] = newFriends
                    updateFriends.add(it)
                }
            }
        }
    }

    fun options(userId: Long,vars: Map<String,Any> = emptyMap()):InvocationParameters{
        val map = hashMapOf<String,Any>()
        map["uploaders"] = friendsCache.computeIfAbsent(userId){ uploaders(it) }
        map["user_id"] = userId
        map.putAll(vars)
        return InvocationParameters.from(map)
    }

    private fun uploaders(userId: Long): List<Long> {
        val a = userId.whoShareMe().toMutableList()
        a.add(userId)
        return a
    }

    private fun tryUpdate(user_id: Long,option: ChatOption){
        if (optionCache[user_id] != option){
            optionCache[user_id] = option
            updateFriends.clear()
            cache.clear()
        }
        if (updateFriends.contains(user_id)){
            updateFriends.remove(user_id)
            cache.remove(user_id)
        }
        if (AIAssistantAPI.forceUpdate){
            updateFriends.clear()
            cache.clear()
        }
    }

    fun <T>createStreamChat(user_id: Long,clazz: Class<T>,option: ChatOption): T{
        tryUpdate(user_id,option)
        return cache.computeIfAbsent(user_id){
            val uploaders = friendsCache.computeIfAbsent(user_id){ uploaders(it) }

            //构建助手构建器
            val builder = AiServices.builder<T>(clazz).chatMemoryProvider{ memoryId ->
                MessageWindowChatMemory.builder()
                    .chatMemoryStore(PostgreSQLMemoryStore.create(user_id))
                    .id(memoryId)
                    .maxMessages(100)
                    .build()
            }
            //构建工具
            val tools = arrayListOf<Any>()


            //构建搜寻
            val routes = arrayListOf<ContentRetriever>()
            if (option.enableWebSearch){
                tools.add(WebSearchTool)
            }
            val augmentor = if (option.enableKnowledgeBase){
                if (option.enablePublicKnowledgeBase){
                    routes.add(KnowledgeBase.public.use())
                }
                if (option.enablePrivateKnowledgeBase){
                    routes.add(KnowledgeBase.create(user_id.computeTableIndex()).use())
                }
                if (option.enableShareKnowledgeBase){
                    routes.addAll(uploaders.filter { it != user_id }.map { KnowledgeBase.create(it.computeTableIndex()).use() })
                }

                DefaultRetrievalAugmentor.builder()
                    .queryRouter(DefaultQueryRouter(routes))
                    .executor(executor)
                    .contentInjector(contentInjector)
                    .build()
            }else{
                null
            }

            //构建系统信息
            var systemMessage = RoleServices.getPrompt(user_id,option.role)
            if (augmentor != null) {
                builder.retrievalAugmentor(augmentor)
            }
            builder.systemMessageProvider {  systemMessage }
            builder.streamingChatModel(AIAssistantAPI.streamingChatModel(option.modelName))
            tools.add(DocumentTool)
            if (option.enableTools){
                AIAssistantAPI.getTools(option.toolGroup)?.let { tool ->
                    tools.add(tool)
                }

            }
            builder.tools(tools)
            builder.build()!!
        } as T
    }
    fun <T>createOnceChat(user_id: Long,clazz: Class<T>,option: ChatOption): T{
        tryUpdate(user_id,option)
        return cache.computeIfAbsent(user_id){
            val uploaders = friendsCache.computeIfAbsent(user_id){ uploaders(it) }

            //构建助手构建器
            val builder = AiServices.builder<T>(clazz)
            //构建工具
            val tools = arrayListOf<Any>()


            //构建搜寻
            val routes = arrayListOf<ContentRetriever>()
            if (option.enableWebSearch){
                tools.add(WebSearchTool)
            }
            val augmentor = if (option.enableKnowledgeBase){
                if (option.enablePublicKnowledgeBase){
                    routes.add(KnowledgeBase.public.use())
                }
                if (option.enablePrivateKnowledgeBase){
                    routes.add(KnowledgeBase.create(user_id.computeTableIndex()).use())
                }
                if (option.enableShareKnowledgeBase){
                    routes.addAll(uploaders.filter { it != user_id }.map { KnowledgeBase.create(it.computeTableIndex()).use() })
                }

                DefaultRetrievalAugmentor.builder()
                    .queryRouter(DefaultQueryRouter(routes))
                    .executor(executor)
                    .contentInjector(contentInjector)
                    .build()
            }else{
                null
            }

            //构建系统信息
            var systemMessage = RoleServices.getPrompt(user_id,option.role)
            if (augmentor != null) {
                builder.retrievalAugmentor(augmentor)
            }
            builder.systemMessageProvider {  systemMessage }
            builder.chatModel(AIAssistantAPI.chatModel(option.modelName))
            tools.add(DocumentTool)
            if (option.enableTools){
                AIAssistantAPI.getTools(option.toolGroup)?.let { tool ->
                    tools.add(tool)
                }
            }
            builder.tools(tools)
            builder.build()!!
        } as T
    }
}