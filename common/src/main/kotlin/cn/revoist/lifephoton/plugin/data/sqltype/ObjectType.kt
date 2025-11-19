package cn.revoist.lifephoton.plugin.data.sqltype

import com.google.gson.Gson
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column
import org.ktorm.schema.SqlType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * @author 6hisea
 * @date  2025/1/8 12:02
 * @description: None
 */
fun <T:Any> BaseTable<*>.obj(name: String): Column<T> {
    return registerColumn(name, ObjectType())
}
val gson = Gson()
class ObjectType<T : Any> : SqlType<T>(Types.VARCHAR, "ser_object") {
    override fun doGetResult(rs: ResultSet, index: Int): T? {
        if (rs.getString(index) == null){
            return null
        }
        return gson.fromJson<T>(rs.getString(index).toString(),Any::class.java)
    }

    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: T) {
        ps.setString(index, gson.toJson(parameter))
    }

}