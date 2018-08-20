package me.blzr.aggregator.exception

class ScriptCancelledException: Exception(), AggregatorException {
    override val message: String?
        get() = "E_SCRIPT_CANCELLED"
}
