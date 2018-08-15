package me.blzr.aggregator.exception

class ScriptTimeoutException: Exception(), AggregatorException {
    override val message: String?
        get() = "E_SCRIPT_TIMEOUT"
}
