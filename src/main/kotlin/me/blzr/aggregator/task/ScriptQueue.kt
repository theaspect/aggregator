package me.blzr.aggregator.task

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class ScriptQueue {
    // TODO For java 9 it will be native
    private val watchdog = Executors.newScheduledThreadPool(QUEUE)
    private val executor = Executors.newFixedThreadPool(QUEUE)

    private fun <REQ : Request, RES : Response> addTask(script: ScriptTask<REQ, RES>) {
        val cfuture = CompletableFuture<RES>()

        val future = executor.submit {
            try {
                cfuture.complete(script.execute().get())
            } catch (e: Exception) {
                cfuture.completeExceptionally(e)
            }
        }

        watchdog.schedule({
            if (!cfuture.isDone) {
                cfuture.completeExceptionally(TimeoutException())
                script.cancel()
                future.cancel(true)
            }
        }, SCRIPT_TIMEOUT, TimeUnit.SECONDS)
    }

    companion object {
        const val QUEUE = 20
        const val SCRIPT_TIMEOUT = 10L
    }
}
