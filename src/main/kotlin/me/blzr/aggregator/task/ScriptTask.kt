package me.blzr.aggregator.task

import com.google.gson.Gson
import me.blzr.aggregator.exception.ScriptErrorException
import me.blzr.aggregator.exception.ScriptTimeoutException
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

abstract class ScriptTask<REQ : ScriptTask.Request, RES : ScriptTask.Response>(private val request: REQ) {
    private val log = LoggerFactory.getLogger(ScriptTask::class.java)

    private var process: Process? = null
    private var state: State = State.PENDING

    abstract fun getScript(): List<String>
    abstract fun parse(input: String): RES

    private fun getInput(): String = Gson().toJson(request)

    @Synchronized
    private fun changeState(target: State, condition: (state: State) -> Boolean = { true }): Boolean {
        return if (condition(this.state)) {
            this.state = target
            true
        } else {
            false
        }
    }

    fun execute(): Supplier<RES> {
        if (changeState(State.RUNNING) { it == State.PENDING }) {
            log.info("Schedule new task")
            return Supplier {
                log.info("Execute task")
                val ps = ProcessBuilder(getScript()).start()
                this.process = ps // Cache process value to be managed externally

                ps.outputStream.bufferedWriter().use {
                    val input = getInput()
                    it.write(input)
                    it.close()
                }

                val response = ps.inputStream.bufferedReader().readText()

                // It's safe because watchdog will kill this process eventually
                val exitCode = ps.waitFor()
                if (exitCode > 0) {
                    val stderr = ps.errorStream.bufferedReader().readText()
                    log.info("Output from script $stderr")
                    throw ScriptErrorException()
                }

                changeState(State.FINISHED) { it == State.RUNNING }
                return@Supplier parse(response)
            }
        } else {
            log.info("Task timeout")
            throw ScriptTimeoutException()
        }
    }

    fun cancel() {
        log.info("Cancel task")
        changeState(State.DESTROYED)
        process?.destroy() // SIGTERM(15)
        process?.waitFor(1, TimeUnit.SECONDS)
        process?.destroyForcibly() // SIGKILL(9)
        process?.waitFor(1, TimeUnit.SECONDS)
    }

    enum class State {
        PENDING, RUNNING, FINISHED, DESTROYED
    }

    interface Request

    interface Response
}
