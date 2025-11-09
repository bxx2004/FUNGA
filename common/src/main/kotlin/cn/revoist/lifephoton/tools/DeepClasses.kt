package cn.revoist.lifephoton.tools

/**
 * @author 6hisea
 * @date  2025/11/7 18:41
 * @description: None
 */
fun <T>Any.getProperty(key:String):T?{
    val d= this::class.java.getDeclaredField(key)
    d.isAccessible = true
    return d.get(this) as T?
}
fun Any.setProperty(key:String,value:Any?){
    val d =this::class.java.getDeclaredField(key)
    d.isAccessible = true
    d.set(this,value)
}
fun Any.properties():List<String>{
    return this::class.java.declaredFields.map{it.name}
}