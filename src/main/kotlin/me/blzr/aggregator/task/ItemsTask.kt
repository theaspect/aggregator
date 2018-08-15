package me.blzr.aggregator.task

import com.google.gson.Gson
import me.blzr.aggregator.exception.ItemsJsonException

class ItemsTask(request: ItemsRequest) :
        ScriptTask<ItemsTask.ItemsRequest, ItemsTask.ItemsResponse>(request) {

    override fun getScript(): List<String> = ITEMS_SCRIPT
    override fun parse(input: String): ItemsResponse {
        val json = Gson().fromJson(input, Map::class.java)
        return if (json.containsKey(ItemsTask.ITEMS_FIELD) && json[ItemsTask.ITEMS_FIELD] is List<*>) {
            if (json.containsKey(ItemsTask.SUPPLIERS_FIELD) && json[ItemsTask.SUPPLIERS_FIELD] is List<*>) {
                ItemsResponse(
                        json[ItemsTask.SUPPLIERS_FIELD] as List<*>,
                        json[ItemsTask.ITEMS_FIELD] as List<*>)
            } else {
                ItemsResponse(
                        json[ItemsTask.SUPPLIERS_FIELD] as List<*>)
            }
        } else {
            throw ItemsJsonException()
        }
    }

    class ItemsRequest(params: Any) : Request
    class ItemsResponse(val items: List<*>, val suppliers: List<*> = emptyList<Any>()) : Response

    companion object {
        const val PATH = "/Users/theaspect/Workspace/aggregator/script"
        const val SUPPLIERS_FIELD = "suppliers"
        const val ITEMS_FIELD = "items"
        val ITEMS_SCRIPT = listOf("/usr/bin/php", "-d", "display_errors=on", "$PATH/items.php")
    }
}
