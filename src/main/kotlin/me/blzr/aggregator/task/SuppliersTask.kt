package me.blzr.aggregator.task

import com.google.gson.Gson
import me.blzr.aggregator.Config
import me.blzr.aggregator.exception.SuppliersJsonException
import me.blzr.aggregator.exception.SuppliersResponseException
import org.slf4j.LoggerFactory

/**
 * This task receive params either from request or from recursive items
 * Return list of suppliers
 */
class SuppliersTask(
        taskId: Long,
        private val config: Config,
        private val request: SuppliersRequest) :
        ScriptTask<SuppliersTask.SuppliersRequest, SuppliersTask.SuppliersResponse>(taskId, request) {
    private val log = LoggerFactory.getLogger(SuppliersTask::class.java)

    override fun getTaskName() = "SUPPLIERS"
    override fun getScript(): List<String> = config.script.suppliers.split(" ")
    override fun parse(input: String): SuppliersResponse {
        val json = try {
            Gson().fromJson(input, Map::class.java)
        } catch (e: Exception) {
            log.error("Can't parse response $this: $input")

            throw SuppliersJsonException()
        }
        try {
            if (json.containsKey(config.fields.suppliers) && json[config.fields.suppliers] is List<*>) {
                return SuppliersResponse(json[config.fields.suppliers] as List<Map<String, *>>)
            } else {
                log.error("Can't parse response $this: $input")

                throw SuppliersResponseException()
            }
        } catch (e: Exception) {
            log.error("Can't parse response $this: $input")

            throw SuppliersResponseException()
        }
    }

    data class SuppliersRequest(val params: Map<String, String>)
    data class SuppliersResponse(val items: List<Map<String, *>>)

    override fun toString(): String = "Suppliers Task#$taskId: $state $request"
}
