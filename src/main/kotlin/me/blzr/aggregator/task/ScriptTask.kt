package me.blzr.aggregator.task

import com.google.gson.Gson
import java.util.function.Supplier

abstract class ScriptTask<REQ : Request, RES : Response>(private val request: REQ) {
    fun getInput(): String = Gson().toJson(request)

    abstract fun getScript(): String
    abstract fun parse(input: String): RES

    fun execute(): Supplier<RES> {
        return Supplier {
            parse("TODO")
        }
    }

    fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
