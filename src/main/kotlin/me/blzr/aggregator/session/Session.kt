package me.blzr.aggregator.session

import com.google.gson.Gson
import me.blzr.aggregator.exception.IllegalRequestException
import me.blzr.aggregator.exception.RequestJsonException
import me.blzr.aggregator.fromJson
import me.blzr.aggregator.task.ScriptTask
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.*

class Session(
        val session: WebSocketSession,
        val message: TextMessage) {
    private val log = LoggerFactory.getLogger(Session::class.java)

    private var isDestroyed = true
    // TODO We should somehow remove finished tasks
    private val tasks: MutableList<ScriptTask<*, *>> = Collections.synchronizedList(mutableListOf())

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

    @Synchronized
    fun isOpen() = session.isOpen

    @Synchronized
    fun isAlive() = isOpen() && !isDestroyed

    @Synchronized
    fun destroy() {
        log.info("Destroy session")
        isDestroyed = true
        session.close(CloseStatus.NORMAL)
        tasks.forEach { it.cancel() }
    }

    @Synchronized
    fun addTask(vararg task: ScriptTask<*, *>) {
        log.info("Register task in session")

        if (!isAlive()) {
            // No need to execute already closed or time out sessions
            tasks.forEach { it.cancel() }
        }

        this.tasks.addAll(task)
    }

    companion object {
        // TODO extract config
        val ALLOWED_LIST = listOf("code", "brand", "apikey", "analog")
    }
}
