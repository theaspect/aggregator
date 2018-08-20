package me.blzr.aggregator.task

import com.google.gson.Gson
import me.blzr.aggregator.exception.ScriptCancelledException
import me.blzr.aggregator.exception.ScriptErrorException
import me.blzr.aggregator.exception.ScriptStartException
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

abstract class ScriptTask<REQ, RES>(val taskId: Long, private val request: REQ) {
    private val log = LoggerFactory.getLogger(ScriptTask::class.java)

    private var process: Process? = null
    protected var state: State = State.PENDING

    abstract fun getScript(): List<String>
    abstract fun parse(input: String): RES

    private fun getInput(): String = Gson().toJson(request)

    fun execute(): Supplier<RES> {
        // Preserve memory, don't schedule already cancelled tasks
        synchronized(state) {
            if (state != State.PENDING) {
                log.error("Script cancelled $this")
                throw ScriptCancelledException()
            }
        }

        log.debug("Schedule $this")
        return Supplier {
            val script = getScript()
            log.debug("Execute $this")
            val ps = synchronized(state) {
                log.debug("Locked $this")
                if (this.state == State.PENDING) {
                    val ps = try {
                        ProcessBuilder(script).start()
                    } catch (e: Exception) {
                        log.error("Can't execute script", e)
                        throw ScriptStartException()
                    }

                    this.state = State.RUNNING
                    this.process = ps

                    log.debug("Script started $this: ${this.process}")
                    return@synchronized ps
                } else {
                    log.error("Script cancelled $this")
                    throw ScriptCancelledException()
                }
            }
            log.debug("Unlocked $this")

            ps.outputStream.bufferedWriter().use {
                val input = getInput()
                it.write(input)
                it.close()
            }

            val response = ps.inputStream.bufferedReader().readText()

            // It's safe because watchdog will kill this process eventually
            val exitCode = ps.waitFor()
            if (exitCode > 0) {
                // 143 means 15
                log.error("Exit code from $this: $exitCode")
                try {
                    val stderr = ps.errorStream.bufferedReader().readText()
                    log.debug("Output from script:\n$stderr")
                } catch (e: Exception) {
                    // We can't know if stream was closed
                    log.debug("Can't read stderr from script")
                }
                throw ScriptErrorException()
            }

            synchronized(state) {
                log.debug("Locked $this")
                if (state == State.RUNNING) {
                    state = State.FINISHED
                }
            }

            log.debug("Task finished $this")
            return@Supplier parse(response)
        }
    }

    fun cancel() {
        synchronized(state) {
            log.debug("Cancel $this")
            if (process?.isAlive == true) {
                process?.destroy() // SIGTERM(15)
                process?.waitFor(1, TimeUnit.SECONDS)
                process?.destroyForcibly() // SIGKILL(9)
                process?.waitFor(1, TimeUnit.SECONDS)
                log.debug("Process $this: ${process?.isAlive}")
            } else {
                log.debug("Process already destroyed or not started $this: ${process?.isAlive}")
            }
            this.state = State.DESTROYED
        }
        log.debug("Unlocked $this")
    }

    enum class State {
        PENDING, RUNNING, FINISHED, DESTROYED
    }
}
