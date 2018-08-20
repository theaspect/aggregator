package me.blzr.aggregator.task

import com.google.gson.Gson
import me.blzr.aggregator.Config
import me.blzr.aggregator.exception.ItemsJsonException
import me.blzr.aggregator.exception.ItemsResponseException
import org.slf4j.LoggerFactory

class ItemsTask(
        taskId: Long,
        private val config: Config,
        private val request: Any) :
        ScriptTask<Any, ItemsTask.ItemsResponse>(taskId, request) {
    private val log = LoggerFactory.getLogger(ItemsTask::class.java)

    override fun getScript(): List<String> = config.script.items.split(" ")
    override fun parse(input: String): ItemsResponse {
        val json = try {
            Gson().fromJson(input, Map::class.java)
        } catch (e: Exception) {
            log.error("Can't parse response $this: $input")

            throw ItemsJsonException()
        }
        return try {
            if (json.containsKey(config.fields.items) && json[config.fields.items] is List<*>) {
                if (json.containsKey(config.fields.suppliers) && json[config.fields.suppliers] is List<*>) {
                    ItemsResponse(
                            json[config.fields.items] as List<*>,
                            json[config.fields.suppliers] as List<*>)
                } else {
                    ItemsResponse(
                            json[config.fields.items] as List<*>)
                }
            } else {
                log.error("Incorrect format $this: $input")

                throw ItemsResponseException()
            }
        } catch (e: Exception) {
            log.error("Incorrect format $this: $input")

            throw ItemsResponseException()
        }
    }

    class ItemsResponse(val items: List<*>, val suppliers: List<*> = emptyList<Any>())

    override fun toString(): String = "Items Task#$taskId: $state $request"
}
