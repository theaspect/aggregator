package me.blzr.aggregator

import me.blzr.aggregator.exception.AggregatorException
import me.blzr.aggregator.exception.UnrecognizedException
import me.blzr.aggregator.session.Session
import me.blzr.aggregator.session.SessionRegistry
import me.blzr.aggregator.task.ItemsTask
import me.blzr.aggregator.task.ScriptQueue
import me.blzr.aggregator.task.ScriptTask
import me.blzr.aggregator.task.SuppliersTask
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Function

@Component
class BusinessLogic(
        val config: Config,
        val sessionRegistry: SessionRegistry,
        val scriptQueue: ScriptQueue) {
    private final val log = LoggerFactory.getLogger(BusinessLogic::class.java)
    private final val executor = Executors.newFixedThreadPool(2, NamedThread("business-logic"))

    val sessionCount = AtomicLong(0)
    val sessionFinished = AtomicLong(0)
    val taskId = AtomicLong(0)
    val taskFailCount = AtomicLong(0)
    val taskSuccessCount = AtomicLong(0)
    val start = System.currentTimeMillis()

    init {
        executor.submit {
            // Begin pipeline when new session registered in #newSession
            // This is blocking await
            while (true) {
                suppliers(sessionRegistry.awaitSession())
            }
        }
    }

    /**
     * Ne session opened by browser
     * See WebSocketHandler#handleTextMessage
     *
     * TODO Should we create two queues?
     */
    fun newSession(session: WebSocketSession, message: TextMessage) {
        executor.submit {
            val s = Session(config, session, message)
            sessionCount.incrementAndGet()
            sessionRegistry.addSession(s)
            s.completableFuture
                    .thenApplyAsync(Function { normal: Boolean ->
                        // In case session closed normally i.e. no more tasks
                        log.error("Session completed $session $normal")
                        sessionFinished.incrementAndGet()
                        s.close(normal)
                    }, executor)
                    .exceptionally { e ->
                        // In case session closed by browser or by timeout
                        // See SessionRegistry#addSession
                        log.error("Session completed exceptionally $session", e)
                        // E is j.u.c.CompletionException
                        sendError(s, null, e.cause ?: e)
                        s.fail(e)
                        sessionFinished.incrementAndGet()
                        s.close(false)
                    }
        }
    }

    /**
     * Connection closed by browser
     * See WebSocketHandler#afterConnectionClosed
     */
    fun closeSession(session: WebSocketSession, status: CloseStatus) {
        executor.submit { sessionRegistry.close(session) }
    }

    /**
     * 1. Generate onSuppliers task from onRequest queue
     */
    private fun suppliers(session: Session) {
        log.debug("Suppliers step $session")
        val task = SuppliersTask(taskId.incrementAndGet(), config, SuppliersTask.SuppliersRequest(session.params))
        session.addTask(task)
        scriptQueue
                .addTask(task)
                .thenApplyAsync(Function<SuppliersTask.SuppliersResponse, Unit> { res ->
                    taskSuccessCount.incrementAndGet()
                    items(session, res)
                }, executor)
                .exceptionally { e ->
                    log.error("Error in suppliers $session", e)
                    taskFailCount.incrementAndGet()
                    // E is j.u.c.CompletionException
                    sendError(session, null, e.cause ?: e)
                }.thenApply { session.removeTask(task) }
    }

    /**
     * 2. Generate one task per supplier from onSuppliers response
     */
    private fun items(session: Session, suppliers: SuppliersTask.SuppliersResponse) {
        log.debug("Items step $session")
        suppliers.items.forEach { item ->
            val task = ItemsTask(taskId.incrementAndGet(), config, item,
                    info = if (item.containsKey(config.fields.info)) {
                        item[config.fields.info]
                    } else {
                        null
                    })
            session.addTask(task)
            sendStatus(session, task, Status.PENDING)
            scriptQueue
                    .addTask(task)
                    .thenApplyAsync(Function<ItemsTask.ItemsResponse, Unit> { res ->
                        taskSuccessCount.incrementAndGet()
                        response(session, task, res)
                        sendStatus(session, task, Status.SUCCESS)
                    }, executor)
                    .exceptionally { e ->
                        log.error("Error in item $session", e.cause)
                        taskFailCount.incrementAndGet()
                        // E is j.u.c.CompletionException
                        sendError(session, task, e.cause ?: e, task.info)
                    }.thenApply { session.removeTask(task) }
        }
    }

    /**
     * 3. Send response responses
     */
    private fun response(session: Session, task: ItemsTask, items: ItemsTask.ItemsResponse) {
        log.debug("Items Response $session")
        items.items.forEach { item ->
            sendItem(session, task, item)
        }

        // Add suppliers if any for recursive request
        items(session, SuppliersTask.SuppliersResponse(items.suppliers))
    }

    private fun sendItem(session: Session, task: ItemsTask, item: Map<String, *>) {
        log.debug("Sent Item to $session")
        session.sendMessage(toMessage(session, task, Status.SENDING, info = task.info).plus(item))
    }

    private fun sendStatus(session: Session, task: ItemsTask, status: Status) {
        log.debug("In session $session $task : $status")
        session.sendMessage(toMessage(session, task, status, info = task.info))
    }

    /**
     * Try to send all available params from session
     */
    private fun sendError(session: Session, task: ScriptTask<*, *>?, e: Throwable, info: Any? = null) {
        val response = toMessage(session, task, Status.ERROR, config.fields.faulty, info, e)
        log.warn("Send Error to $session: $response")
        session.sendMessage(response)
    }

    fun uptime(): String =
            Duration.ofMillis(System.currentTimeMillis() - start)
                    .toString()
                    .substring(2)
                    .replace("(\\d+)(\\.\\d+)?([HMS])".toRegex(), "$1$3 ")
                    .toLowerCase()
                    .trim()

    fun getStats() = mapOf(
            "uptime" to uptime(),
            "sessionTotal" to sessionCount.get(),
            "sessionAlive" to sessionRegistry.alive(),
            "sessionFinished" to sessionFinished.get(),
            "taskTotal" to taskId.get(),
            "taskPending" to sessionRegistry.tasks(),
            "taskSucceeded" to taskSuccessCount.get(),
            "taskFailed" to taskFailCount.get()
            )

    fun toMessage(session: Session,
                  task: ScriptTask<*, *>?,
                  status: Status,
            // Params is get from session request
                  params: List<String> = listOf(),
            // Info is tekan from suppliers response
                  info: Any? = null,
                  e: Throwable? = null
    ): Map<String, Any?> {
        var response: Map<String, Any?> = mapOf(
                "_session" to session.id,
                "_task" to (task?.getTaskName() ?: "UNKNOWN"),
                "_id" to (task?.taskId ?: ""),
                "_status" to status.toString())

        if (params.isNotEmpty()) {
            val filteredParams = session.params.filterKeys { params.contains(it) }
            if (filteredParams.isNotEmpty()) {
                response += "_params" to filteredParams
            }
        }

        if (info != null) {
            response += ("_info" to info)
        }

        if (status == Status.ERROR) {
            response += (
                    "_error" to
                            if (e is AggregatorException) {
                                e.message
                            } else {
                                log.error("Unknown exception", e)
                                UnrecognizedException().message
                            })
        }

        return response
    }

    enum class Status {
        PENDING, SENDING, SUCCESS, ERROR
    }
}
