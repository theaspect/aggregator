package me.blzr.aggregator.task

import me.blzr.aggregator.Config
import me.blzr.aggregator.NamedThread
import me.blzr.aggregator.exception.ScriptTimeoutException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class ScriptQueue(
        private val config: Config) {
    private val log = LoggerFactory.getLogger(ScriptQueue::class.java)
    // TODO For java 9 it will be native
    private val watchdog = Executors.newScheduledThreadPool(config.pool.watchdog, NamedThread("script-watchdog"))
    private val executor = Executors.newFixedThreadPool(config.pool.executor, NamedThread("script-pool"))

    fun <REQ, RES> addTask(script: ScriptTask<REQ, RES>): CompletableFuture<RES> {
        log.debug("Add $script")
        val cFuture = CompletableFuture<RES>()

        val future = executor.submit {
            try {
                cFuture.complete(script.execute().get())
            } catch (e: Exception) {
                log.error("Completed exceptionally $script", e)
                cFuture.completeExceptionally(e)
            }
        }

        watchdog.schedule({
            if (!cFuture.isDone) {
                log.warn("Timeout $script")
                cFuture.completeExceptionally(ScriptTimeoutException())
                script.cancel()
                future.cancel(false)
            }
        }, config.timeout.script, TimeUnit.SECONDS)

        return cFuture
    }
}
