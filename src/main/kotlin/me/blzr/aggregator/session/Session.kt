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
        private val config: Config,
        val session: WebSocketSession,
        private val message: TextMessage) {
    private val log = LoggerFactory.getLogger(Session::class.java)

    private val age = System.currentTimeMillis()
    private var isDestroyed = false
    private val tasks: MutableList<ScriptTask<*, *>> = Collections.synchronizedList(mutableListOf())

    val id = session.id
    val params: Map<String, String>
    val completableFuture = CompletableFuture<Boolean>()

    init {
        this.params = getAllowedParams()
    }

    private fun getAllowedParams(): Map<String, String> {
        return try {
            val request: Map<String, String> = Gson().fromJson(message.payload)

            if (!request.keys.containsAll(config.fields.request)) {
                log.error("Missed required params $session")
                fail(IllegalRequestException())
            }

            request
        } catch (e: Exception) {
            // No need to print whole stack trace
            log.error("Can't parse params in $session: ${e.message}")
            fail(RequestJsonException(e))
            mapOf()
        }
    }

    @Synchronized
    fun isOpen() = session.isOpen

    @Synchronized
    fun isAlive() = isOpen() && !isDestroyed

    @Synchronized
    fun failByWebsocket(ws: WebSocketSession, e: Throwable) {
        if (ws == this.session) { fail(e) }
    }

    @Synchronized
    fun fail(e: Throwable) {
        if (isDestroyed && tasks.isEmpty()) {
            // No need to do anything if we receive event from web socket
            return
        }

        log.debug("Fail $this")
        isDestroyed = true

        tasks.removeAll { task ->
            task.cancel()
            return@removeAll true
        }

        completableFuture.completeExceptionally(e)
    }

    @Synchronized
    private fun complete(done: Boolean) {
        completableFuture.complete(done)
    }

    @Synchronized
    fun addTask(vararg task: ScriptTask<*, *>) {
        log.debug("Register task in $this")

        if (isAlive()) {
            this.tasks.addAll(task)
        } else {
            log.debug("Skip register task in dead session $this")
        }
    }

    @Synchronized
    fun removeTask(task: ScriptTask<*, *>) {
        log.debug("Remove $task in $this")
        tasks.remove(task)

        if (tasks.isEmpty()) {
            log.info("No more tasks in $this, closing")
            complete(true)
        }
    }

    @Synchronized
    fun sendMessage(item: Any) {
        val json = Gson().toJson(item)
        if (session.isOpen) {
            session.sendMessage(TextMessage(json))
        } else {
            log.warn("Can't send response to closed session $this: $json")
        }
    }

    @Synchronized
    fun close(normal: Boolean) {
        if (normal) {
            session.close(CloseStatus.NORMAL)
        } else {
            session.close(CloseStatus.SERVER_ERROR)
        }
    }

    fun tasks() = tasks.size

    override fun toString(): String {
        return "Session#${session.id} params: $params tasks: ${tasks.size} open: ${isOpen()} destroyed: $isDestroyed age: ${System.currentTimeMillis() - age}"
    }
}
