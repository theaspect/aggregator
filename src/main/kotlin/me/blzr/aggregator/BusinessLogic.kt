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
            // This is blocking await
            while (true) {
                suppliers(sessionRegistry.awaitSession())
            }
        }
    }

    // Should we create two queues
    fun newSession(session: WebSocketSession, message: TextMessage) {
        executor.submit {
            val s = Session(config, session, message)
            sessionCount.incrementAndGet()
            sessionRegistry.addSession(s)
            s.completableFuture
                    .thenApplyAsync(Function { normal: Boolean ->
                        log.error("Session completed $session $normal")
                        sessionFinished.incrementAndGet()
                        s.close(normal)
                    }, executor)
                    .exceptionally { e ->
                        log.error("Session completed exceptionally $session", e)
                        // E is j.u.c.CompletionException
                        sendError(s, null, e.cause ?: e)
                        s.fail(e)
                        sessionFinished.incrementAndGet()
                        s.close(false)
                    }
        }
    }

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
     * 2. Generate response task from onSuppliers
     */
    private fun items(session: Session, suppliers: SuppliersTask.SuppliersResponse) {
        log.debug("Items step $session")
        suppliers.items.filterNotNull().forEach { item ->
            val task = ItemsTask(taskId.incrementAndGet(), config, item)
            session.addTask(task)
            scriptQueue
                    .addTask(task)
                    .thenApplyAsync(Function<ItemsTask.ItemsResponse, Unit> { res ->
                        taskSuccessCount.incrementAndGet()
                        response(session, res)
                    }, executor)
                    .exceptionally { e ->
                        log.error("Error in item $session", e.cause)
                        taskFailCount.incrementAndGet()
                        // E is j.u.c.CompletionException
                        sendError(session, task, e.cause ?: e)
                    }.thenApply { session.removeTask(task) }
        }
    }

    /**
     * 3. Send response responses
     */
    private fun response(session: Session, items: ItemsTask.ItemsResponse) {
        log.debug("Items Response $session")
        items.items.filterNotNull().forEach { item ->
            sendItem(session, item)
        }

        // Add suppliers if any for recursive request
        items(session, SuppliersTask.SuppliersResponse(items.suppliers))
    }

    private fun sendItem(session: Session, item: Any) {
        log.debug("Sent Item to $session")
        session.sendMessage(item)
    }

    /**
     * Try to send all available params from session
     */
    private fun sendError(session: Session, task: ScriptTask<*, *>?, e: Throwable) {
        val response = if (e is AggregatorException) {
            session.params.filterKeys { config.fields.faulty.contains(it) }
                    .plus("session" to session.id)
                    .plus("task" to (task?.taskId ?: ""))
                    .plus("error" to e.message)
        } else {
            log.error("Unknown exception", e)
            session.params.filterKeys { config.fields.faulty.contains(it) }
                    .plus("id" to session.id)
                    .plus("task" to (task?.taskId ?: ""))
                    .plus("error" to UnrecognizedException().message)
        }

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
}
