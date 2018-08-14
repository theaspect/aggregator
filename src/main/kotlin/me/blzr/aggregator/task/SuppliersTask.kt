package me.blzr.aggregator.task

import com.google.gson.Gson
import me.blzr.aggregator.exception.SuppliersJsonException

class SuppliersTask(request: SuppliersRequest) :
        ScriptTask<SuppliersTask.SuppliersRequest, SuppliersTask.SuppliersResponse>(request) {

    override fun getScript(): List<String> = SUPPLIERS_SCRIPT
    override fun parse(input: String): SuppliersResponse {
        val json = Gson().fromJson(input, Map::class.java)
        if (json.containsKey(SUPPLIERS_FIELD) && json[SUPPLIERS_FIELD] is List<*>) {
            return SuppliersResponse(json[SUPPLIERS_FIELD] as List<*>)
        } else {
            throw SuppliersJsonException()
        }
    }


    data class SuppliersRequest(val params: Map<String, String>) : Request
    data class SuppliersResponse(val items: List<*>) : Response

    companion object {
        const val PATH = "/Users/theaspect/Workspace/aggregator/script"
        val SUPPLIERS_SCRIPT = listOf("/usr/bin/php", "$PATH/suppliers.php")
        const val SUPPLIERS_FIELD = "suppliers"
    }
}
