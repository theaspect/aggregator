package me.blzr.aggregator.session

import com.google.gson.Gson
import me.blzr.aggregator.Config
import me.blzr.aggregator.exception.IllegalRequestException
import me.blzr.aggregator.exception.RequestJsonException
import me.blzr.aggregator.fromJson
import me.blzr.aggregator.task.ScriptTask
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.*
import java.util.concurrent.CompletableFuture

class Session(
        val config: Config,
        val session: WebSocketSession,
        val message: TextMessage) {
    private val log = LoggerFactory.getLogger(Session::class.java)

    private var isDestroyed = true
    // TODO We should somehow remove finished tasks
    private val tasks: MutableList<ScriptTask<*, *>> = Collections.synchronizedList(mutableListOf())

    val params: Map<String, String>
    val completableFuture = CompletableFuture<Boolean>()

    init {
        this.params = getAllowedParams()
    }

    private fun getAllowedParams(): Map<String, String> {
        val request: Map<String, String> = try {
            Gson().fromJson(message.payload)
        } catch (e: Exception) {
            log.error("Can't parse params in $session", e)
            throw RequestJsonException(e)
        }

        if (!request.keys.containsAll(config.fields.request)) {
            log.error("Missed required params $session")
            throw IllegalRequestException()
        }

        return request
    }

    @Synchronized
    fun isOpen() = session.isOpen

    @Synchronized
    private fun isAlive() = isOpen() && !isDestroyed

    @Synchronized
    fun destroy() {
        log.debug("Destroy $this")
        isDestroyed = true
        session.close(CloseStatus.NORMAL)
        tasks.forEach { it.cancel() }

        completableFuture.complete(false)
    }

    @Synchronized
    fun addTask(vararg task: ScriptTask<*, *>) {
        log.debug("Register task in $this")

        if (!isAlive()) {
            // No need to execute already closed or time out sessions
            tasks.forEach { it.cancel() }
        }

        this.tasks.addAll(task)
    }

    @Synchronized
    fun removeTask(task: ScriptTask<*, *>) {
        log.debug("Remove $task in $this")
        tasks.remove(task)

        if (tasks.isEmpty()) {
            log.debug("No mere tasks in $this, closing")
            completableFuture.complete(true)
        }
    }

    override fun toString(): String {
        return "Session#${session.id} params: $params tasks: ${tasks.size} open: ${isOpen()} destroyed: $isDestroyed"
    }
}
