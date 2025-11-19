package cn.revoist.lifephoton.module.funga.data

import cn.revoist.lifephoton.module.funga.core.genephenotype.model.AnalysisResult
import cn.revoist.lifephoton.plugin.data.sqltype.gson
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column
import org.ktorm.schema.SqlType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
fun BaseTable<*>.art(name: String): Column<AnalysisResult> {
    return registerColumn(name, AnalysisResultType())
}
class AnalysisResultType : SqlType<AnalysisResult>(Types.VARCHAR, "art") {
    override fun doGetResult(rs: ResultSet, index: Int): AnalysisResult? {
        if (rs.getString(index) == null){
            return null
        }
        return gson.fromJson(rs.getString(index).toString(),AnalysisResult::class.java) as AnalysisResult
    }

    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: AnalysisResult) {
        ps.setString(index, gson.toJson(parameter))
    }

}