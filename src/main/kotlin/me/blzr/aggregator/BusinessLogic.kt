package me.blzr.aggregator

import com.google.gson.Gson
import me.blzr.aggregator.session.Session
import me.blzr.aggregator.session.SessionRegistry
import me.blzr.aggregator.task.ItemsTask
import me.blzr.aggregator.task.ScriptQueue
import me.blzr.aggregator.task.SuppliersTask
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.Executors
import java.util.function.Function

@Component
class BusinessLogic(
        val sessionRegistry: SessionRegistry,
        val scriptQueue: ScriptQueue) {
    private final val executor = Executors.newSingleThreadExecutor()

    init {
        executor.submit(::onRequest)
    }

    // Should we create two queues
    fun newSession(session: WebSocketSession, message: TextMessage) {
        sessionRegistry.addSession(Session(session, message))
    }

    /**
     * 1. Generate onSuppliers task from onRequest queus
     */
    private fun onRequest() {
        while (true) {
            // TODO Does this block executor service?
            val session = sessionRegistry.getSession()
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
    }

    /**
     * 2. Generate onItems task from onSuppliers
     */
    private fun onSuppliers(session: Session, suppliers: SuppliersTask.SuppliersResponse) {
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
        items.items.filterNotNull().forEach { item ->
            sendItem(session, item)
        }

        // Add suppliers if any for recursive request
        onSuppliers(session, SuppliersTask.SuppliersResponse(items.suppliers))
    }

    private fun sendItem(session: Session, item: Any) {
        session.session.sendMessage(TextMessage(Gson().toJson(item)))
    }

    /**
     * Try to send all available params from session
     */
    private fun sendError(session: Session, e: Throwable) {
        val response = session.getAllowedParams().filterKeys { FAULTY.contains(it) }
                .plus("error" to e.message)
        session.session.sendMessage(TextMessage(Gson().toJson(response)))
    }

    companion object {
        // TODO configure
        val FAULTY = listOf("code", "brand", "analog")
    }
}
