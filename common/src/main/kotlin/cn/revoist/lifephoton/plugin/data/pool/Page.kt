package cn.revoist.lifephoton.plugin.data.pool

import cn.revoist.lifephoton.plugin.route.PagingPayloadResponse
import kotlin.math.ceil

interface Page{
    fun toResponse() :PagingPayloadResponse<*>
    fun toResponse(index:Int) :PagingPayloadResponse<*>
}
/**
 * @author 6hisea
 * @date  2025/1/9 15:49
 * @description: None
 */
data class NormalPage(val pagination:Int,val allPagination:Int,val data:List<Any?>,val session:String,var lock:Boolean):Page{
    override fun toResponse():PagingPayloadResponse<*>{
        val pre = if (pagination != 1){
            pagination - 1
        }else{
            -1
        }
        val next = if (pagination >= allPagination){
            -1
        }else{
            pagination +1
        }
        return PagingPayloadResponse(true,"success",pre,next,allPagination,session,data)
    }

    override fun toResponse(index:Int): PagingPayloadResponse<*> {
        return toResponse()
    }
}

data class DynamicPageInformation(
    val data:List<Any?>,
    val allPages:Int,
    val pre:Int = -1,
    val next:Int = -1,
)

data class DynamicPage(
    val size: Int,
    val session:String,
    val func:(pagination: Int,size: Int) -> DynamicPageInformation
):Page{
    override fun toResponse():PagingPayloadResponse<*>{
        val data = func(0,10)
        return PagingPayloadResponse(true,"success",-1,-1,1,session,data.data)
    }

    override fun toResponse(index: Int): PagingPayloadResponse<*> {
        val generator = func(index,size)
        val data = generator.data
        val pre = generator.pre
        val next = generator.next
        return PagingPayloadResponse(true,"success",pre,next,generator.allPages,session,data)
    }
}

fun splitPage(data: List<Any?>, count:Int,session: String,lock:Boolean):List<Page> {
    val result = ArrayList<Page>()
    val allPage = ceil((data.size.toDouble() / count.toDouble())).toInt()
    for (i in 0 until allPage) {
        if (data.size <= i*count+count){
            result.add(NormalPage(i+1,allPage,data.subList(i*count,data.size),session,lock))
        }else{
            result.add(NormalPage(i+1,allPage, data.subList(i*count,i*count+count),session,lock))
        }
    }
    if (result.isEmpty()){
        result.add(NormalPage(1,1,arrayListOf(),session,lock))
    }
    return result
}