package cn.revoist.lifephoton.plugin.data

import cn.revoist.lifephoton.Booster
import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.data.pool.BufferPool
import cn.revoist.lifephoton.plugin.data.pool.DynamicPage
import cn.revoist.lifephoton.plugin.data.pool.DynamicPageInformation
import cn.revoist.lifephoton.plugin.data.pool.NormalPage
import cn.revoist.lifephoton.plugin.data.pool.Page
import cn.revoist.lifephoton.plugin.data.pool.TempMemoryContainer
import cn.revoist.lifephoton.plugin.data.pool.splitPage
import cn.revoist.lifephoton.plugin.route.PagingPayloadResponse
import cn.revoist.lifephoton.plugin.route.PayloadResponse
import cn.revoist.lifephoton.tools.submit
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.schema.Table

/**
 * @author 6hisea
 * @date  2025/1/8 10:59
 * @description: None
 */

class DataManager(val plugin:Plugin) {
    private val databases = ArrayList<Database>()
    private val bufferPool = BufferPool(plugin,plugin.id)
    private val pageBuffer = HashMap<String,List<Page>>()
    private val pageCache = HashMap<String,String>()
    private val tempContainer = arrayListOf<TempMemoryContainer<*>>()

    init {
        submit(-1,1000*60*60*6){
            tempContainer.clear()
        }
    }

    fun <T>useTempContainer():TempMemoryContainer<T>{
        tempContainer.add(TempMemoryContainer<T>())
        return tempContainer.last as TempMemoryContainer<T>
    }

    fun useDatabase(dbName:String = plugin.id):Database{
        if (databases.map { it.name }.contains(dbName)) {
            return databases.find { it.name == dbName }!!
        }
        val db = Database.connect(url = "jdbc:postgresql://${Booster.DB_URL}/${dbName}", driver = "org.postgresql.Driver", user = Booster.DB_USERNAME, password = Booster.DB_PASSWORD)
        databases.add(db)
        return db
    }
    private fun generateCode():String{
        var result = ""
        val number = arrayListOf(1,2,3,4,5,6,7,8,9,0)
        for (i in 0 until 6) {
            result += number.random().toString()
        }
        return result
    }
    fun useDefaultDatabase():Database{
        return Booster.database.value!!
    }
    fun useBuffers():BufferPool{
        return bufferPool
    }
    private fun useBuffer(key:String, id:String = "default"):Any?{
        return bufferPool["$id:$key"]
    }
    fun useBuffer(key: String,id: String ="default",defaultFunc:()->Any):Any{
        return useBuffer(key,id)?:defaultFunc()
    }
    fun useBuffer(keyWithId:String,defaultFunc:()->Any):Any{
        val r = useBuffer(keyWithId.split(":")[1],keyWithId.split(":")[0])
        if (r == null){
            bufferPool[keyWithId] = defaultFunc()
        }
        return bufferPool[keyWithId]!!
    }
    fun useBuffer(keyWithId:String,timeout:Int = 1000*60,defaultFunc:()->Any):Any{
        submit(timeout,-1){
            bufferPool.remove(keyWithId)
            it.cancel()
        }
        return useBuffer(keyWithId, defaultFunc)
    }
    fun usePaginationCache(uri:String):String?{
        return pageCache[uri]
    }

    fun useDynamicPagination(size: Int = 20, func:(pagination: Int, size: Int) -> DynamicPageInformation):PagingPayloadResponse<*>{

        val code = "dynamic_${generateCode()}"
        val page = DynamicPage(size,"${plugin.id}-${code}",func)
        pageBuffer[code] = arrayListOf(page)
        return pageBuffer[code]!![0].toResponse(1)
    }

    fun usePagination(data:List<Any>,count:Int = 20,lock:Boolean = false,cache:String = ""):PagingPayloadResponse<*>{
        val code = generateCode()
        val pages = splitPage(data,count,"${plugin.id}-${code}",lock)
        pageBuffer[code] = pages
        if (cache.isNotEmpty()){
            pageCache[cache] = code
        }
        return pages[0].toResponse(1)
    }
    fun getPages(code:String):List<Page>?{
        return pageBuffer[code]
    }
    fun getPage(code:String,num:Int):Page?{
        val page = pageBuffer[code]
        if (page != null){
            if (code.startsWith("dynamic_")){
                return page[0]
            }
            if (num >= 1 && num <= page.size){
                return page.filterIsInstance<NormalPage>().find { it.pagination == num }
            }
        }
        return null
    }

}

fun Entity<*>.toPayloadResponse(message:String = "success",excludes:List<String> = arrayListOf(),joins:HashMap<String,Any?> = hashMapOf()):PayloadResponse<HashMap<String,Any?>>{
    val payload = HashMap<String,Any?>()
    this.properties.forEach { (t, u) ->
        if (!excludes.contains(t)){
            payload[t] = u
        }
    }
    joins.forEach { (t, u) ->
        payload[t] = u
    }
    return PayloadResponse(
        true,message,payload
    )
}