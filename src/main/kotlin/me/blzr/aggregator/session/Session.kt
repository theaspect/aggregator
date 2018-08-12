package me.blzr.aggregator.session

import com.google.gson.Gson
import me.blzr.aggregator.exception.IllegalRequestException
import me.blzr.aggregator.exception.RequestJsonException
import me.blzr.aggregator.fromJson
import me.blzr.aggregator.task.ScriptTask
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

class Session(
        val session: WebSocketSession,
        val message: TextMessage) {

    private val tasks: MutableList<ScriptTask<ScriptTask.Request, ScriptTask.Response>> = mutableListOf()

    fun getAllowedParams(): Map<String, String> {
        val request: Map<String, String> = try {
            Gson().fromJson(message.payload)
        } catch (e: Exception) {
            throw RequestJsonException(e)
        }

        if (!request.keys.containsAll(ALLOWED_LIST)) {
            throw IllegalRequestException()
        }

        return request
    }

    fun isAlive() = session.isOpen
    fun destroy() = tasks.forEach { it.cancel() }

    companion object {
        // TODO extract config
        val ALLOWED_LIST = listOf("code", "brand", "apikey", "analog")
    }
}
