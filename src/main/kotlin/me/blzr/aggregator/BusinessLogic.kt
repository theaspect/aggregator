package me.blzr.aggregator

import com.google.gson.Gson
import me.blzr.aggregator.session.Session
import me.blzr.aggregator.session.SessionRegistry
import me.blzr.aggregator.task.ItemsTask
import me.blzr.aggregator.task.ScriptQueue
import me.blzr.aggregator.task.SuppliersTask
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.Executors
import java.util.function.Function

@Component
class BusinessLogic(
        val config: Config,
        val sessionRegistry: SessionRegistry,
        val scriptQueue: ScriptQueue) {
    private final val log = LoggerFactory.getLogger(BusinessLogic::class.java)
    private final val executor = Executors.newSingleThreadExecutor()

    init {
        executor.submit {
            while (true) {
                onRequest(sessionRegistry.getSession())
            }
        }
    }

    // Should we create two queues
    fun newSession(session: WebSocketSession, message: TextMessage) {
        sessionRegistry.addSession(Session(config, session, message))
    }

    fun closeSession(session: WebSocketSession, status: CloseStatus) {
        // TODO notify session and stop all nested tasks
    }

    /**
     * 1. Generate onSuppliers task from onRequest queus
     */
    private fun onRequest(session: Session) {
        log.info("Starting new session")
        val task = SuppliersTask(SuppliersTask.SuppliersRequest(session.getAllowedParams()))
        session.addTask(task)
        scriptQueue
                .addTask(task)
                .thenApplyAsync(Function<SuppliersTask.SuppliersResponse, Unit> { res -> onSuppliers(session, res) }, executor)
                .exceptionally { e ->
                    sendError(session, e)
                    session.destroy()
                }
    }

    /**
     * 2. Generate onItems task from onSuppliers
     */
    private fun onSuppliers(session: Session, suppliers: SuppliersTask.SuppliersResponse) {
        log.info("Suppliers Response")
        suppliers.items.filterNotNull().forEach { item ->
            val task = ItemsTask(ItemsTask.ItemsRequest(item))
            session.addTask(task)
            scriptQueue
                    .addTask(task)
                    .thenApplyAsync(Function<ItemsTask.ItemsResponse, Unit> { res -> onItems(session, res) }, executor)
                    .exceptionally { e ->
                        sendError(session, e)
                        session.destroy()
                    }
        }
    }

    /**
     * 3. Send onItems responses
     */
    private fun onItems(session: Session, items: ItemsTask.ItemsResponse) {
        log.info("Items Response")
        items.items.filterNotNull().forEach { item ->
            sendItem(session, item)
        }

        // Add suppliers if any for recursive request
        onSuppliers(session, SuppliersTask.SuppliersResponse(items.suppliers))
    }

    private fun sendItem(session: Session, item: Any) {
        log.info("Sent Item")
        session.session.sendMessage(TextMessage(Gson().toJson(item)))
    }

    /**
     * Try to send all available params from session
     */
    private fun sendError(session: Session, e: Throwable) {
        log.info("Send Error")
        val response = session.getAllowedParams().filterKeys { config.fields.faulty.contains(it) }
                .plus("error" to e.message)
        session.session.sendMessage(TextMessage(Gson().toJson(response)))
    }
}
