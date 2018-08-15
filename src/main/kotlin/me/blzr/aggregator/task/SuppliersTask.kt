package me.blzr.aggregator.task

import com.google.gson.Gson
import me.blzr.aggregator.Config
import me.blzr.aggregator.exception.SuppliersJsonException
import me.blzr.aggregator.exception.SuppliersResponseException
import org.slf4j.LoggerFactory

class SuppliersTask(
        private val config: Config,
        private val request: SuppliersRequest) :
        ScriptTask<SuppliersTask.SuppliersRequest, SuppliersTask.SuppliersResponse>(request) {
    private val log = LoggerFactory.getLogger(SuppliersTask::class.java)

    override fun getScript(): List<String> = config.script.suppliers.split(" ")
    override fun parse(input: String): SuppliersResponse {
        val json = try {
            Gson().fromJson(input, Map::class.java)
        } catch (e: Exception) {
            log.error("Can't parse response $this: $input")

            throw SuppliersJsonException()
        }

        if (json.containsKey(config.fields.suppliers) && json[config.fields.suppliers] is List<*>) {
            return SuppliersResponse(json[config.fields.suppliers] as List<*>)
        } else {
            log.error("Can't parse response $this: $input")

            throw SuppliersResponseException()
        }
    }

    data class SuppliersRequest(val params: Map<String, String>) : Request
    data class SuppliersResponse(val items: List<*>) : Response

    override fun toString(): String = "Suppliers Task: $state $request"
}
