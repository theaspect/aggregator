package me.blzr.aggregator.task

import com.google.gson.Gson
import me.blzr.aggregator.fromJson

class ItemsTask(request: ItemsRequest) :
        ScriptTask<ItemsTask.ItemsRequest, ItemsTask.ItemsResponse>(request) {

    override fun getScript(): List<String> = ITEMS_SCRIPT
    override fun parse(input: String): ItemsResponse = Gson().fromJson(input)

    class ItemsRequest : Request
    class ItemsResponse : Response

    companion object {
        val ITEMS_SCRIPT = listOf("php", "./items.php")
    }
}
