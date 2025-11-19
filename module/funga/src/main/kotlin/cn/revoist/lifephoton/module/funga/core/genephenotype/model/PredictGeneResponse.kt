package cn.revoist.lifephoton.module.funga.core.genephenotype.model

/**
 * @author 6hisea
 * @date  2025/4/21 13:57
 * @description: None
 */
class PredictGeneResponse() :HashMap<Int,List<String>>() {
    operator fun plus(dg1: PredictGeneResponse): PredictGeneResponse {
        dg1.forEach { t, u ->
            if (containsKey(t)){
                val r = arrayListOf(*u.toTypedArray())
                r.addAll(u)
                put(t, r)
            }else{
                put(t,u)
            }
        }
        return this
    }
    fun findDegree(gene:String):Int{
        var dge = 0
        for (i in 1..keys.size){
            if (get(i)?.contains(gene) == true){
                dge = i
                break
            }
        }
        return dge
    }
}