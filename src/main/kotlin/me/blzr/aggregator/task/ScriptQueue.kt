package me.blzr.aggregator.task

import me.blzr.aggregator.exception.SessionTimeoutException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class ScriptQueue {
    private val log = LoggerFactory.getLogger(ScriptQueue::class.java)
    // TODO For java 9 it will be native
    private val watchdog = Executors.newScheduledThreadPool(WATCHDOG_POOL)
    private val executor = Executors.newFixedThreadPool(SCRIPT_POOL)

    fun <REQ : ScriptTask.Request, RES : ScriptTask.Response> addTask(script: ScriptTask<REQ, RES>): CompletableFuture<RES> {
        val cFuture = CompletableFuture<RES>()

        val future = executor.submit {
            try {
                cFuture.complete(script.execute().get())
            } catch (e: Exception) {
                cFuture.completeExceptionally(e)
            }
        }

        watchdog.schedule({
            if (!cFuture.isDone) {
                log.info("Task timeout")
                cFuture.completeExceptionally(SessionTimeoutException())
                script.cancel()
                future.cancel(true)
            }
        }, SCRIPT_TIMEOUT, TimeUnit.SECONDS)

        return cFuture
    }

    companion object {
        const val SCRIPT_POOL = 20
        const val WATCHDOG_POOL = 5
        const val SCRIPT_TIMEOUT = 120L // FIXME FOR DEBUG PURPOSES
    }
}
