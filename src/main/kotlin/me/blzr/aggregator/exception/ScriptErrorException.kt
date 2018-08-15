package me.blzr.aggregator.exception

class ScriptErrorException: Exception(), AggregatorException {
    override val message: String?
        get() = "E_SCRIPT_ERROR"
}
