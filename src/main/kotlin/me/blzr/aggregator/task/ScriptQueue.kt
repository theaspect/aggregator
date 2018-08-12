package me.blzr.aggregator.task

import me.blzr.aggregator.exception.SessionTimeoutException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Function

class ScriptQueue {
    // TODO For java 9 it will be native
    private val watchdog = Executors.newScheduledThreadPool(QUEUE)
    private val executor = Executors.newFixedThreadPool(QUEUE)

    private fun <REQ : ScriptTask.Request, RES : ScriptTask.Response> addTask(script: ScriptTask<REQ, RES>) {
        val cFuture = CompletableFuture<RES>()
        cFuture.thenApplyAsync(Function<RES, Unit> { res -> businessLogic(res) }, executor)

        val future = executor.submit {
            try {
                cFuture.complete(script.execute().get())
            } catch (e: Exception) {
                cFuture.completeExceptionally(e)
            }
        }

        watchdog.schedule({
            if (!cFuture.isDone) {
                cFuture.completeExceptionally(SessionTimeoutException())
                script.cancel()
                future.cancel(true)
            }
        }, SCRIPT_TIMEOUT, TimeUnit.SECONDS)
    }

    private fun businessLogic(res: ScriptTask.Response) {
        when (res) {
            is ItemsTask.ItemsResponse -> TODO()
            is SuppliersTask.SuppliersResponse -> TODO()
            else -> throw IllegalStateException("Unknown Task Type $res")
        }
    }

    companion object {
        const val QUEUE = 20
        const val SCRIPT_TIMEOUT = 10L
    }
}
