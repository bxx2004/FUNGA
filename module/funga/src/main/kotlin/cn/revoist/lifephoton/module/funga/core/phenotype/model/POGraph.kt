package cn.revoist.lifephoton.module.funga.core.phenotype.model

import cn.revoist.lifephoton.plugin.data.json.JSONObject
import cn.revoist.lifephoton.plugin.data.json.jsonObject

class TripletList<T,R,Y>: ArrayList<TripletList.Triplet<T, R, Y>>() {
    data class Triplet<T,R,Y>(var left:T,var middle:R,var right:Y)
    fun add(l:T,m:R,r:Y){
        add(Triplet(l,m,r))
    }
}

data class POGraph(val root: String?) {
    private val nodes = HashMap< String,String>()
    private val lines = TripletList<String, String, String>()
    fun toJSONObject(): JSONObject {
        val ns = ArrayList<JSONObject>()
        nodes.forEach {
            ns.add(
                jsonObject{
                    put("id",it.key)
                    put("text",it.value)
                }
            )
        }
        val ls = ArrayList<JSONObject>()
        lines.forEach {
            ls.add(
                jsonObject{
                    put("from",it.left)
                    put("to",it.right)
                    put("text",it.middle)
                 }
            )
        }
        return jsonObject {
            if (root != null){
                put("rootID", root)
            }
            put("nodes", ns)
            put("lines", ls)
        }
    }
    fun addNode(id:String,node: String) {
        if (nodes.contains(node)) return
        nodes.put(id,node)
    }
    fun addLine(from: String,type:String,target: String) {
        if (lines.map { it.left + it.middle + it.right }.contains(from + type + target)) return
        lines.add(from,type,target)
    }
}