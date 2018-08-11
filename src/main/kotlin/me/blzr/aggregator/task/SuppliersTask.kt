package me.blzr.aggregator.task

import com.google.gson.Gson
import me.blzr.aggregator.fromJson

class SuppliersTask(request: SuppliersRequest) :
        ScriptTask<SuppliersTask.SuppliersRequest, SuppliersTask.SuppliersResponse>(request) {

    override fun getScript(): String = SUPPLIERS_SCRIPT
    override fun parse(input: String): SuppliersResponse = Gson().fromJson(input)


    class SuppliersRequest : Request
    class SuppliersResponse : Response


    companion object {
        const val SUPPLIERS_SCRIPT = "/suppliers.php"
    }
}
