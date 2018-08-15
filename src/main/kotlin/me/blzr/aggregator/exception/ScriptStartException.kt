package me.blzr.aggregator.exception

class ScriptStartException: Exception() {
    override val message: String?
        get() = "E_SCRIPT_START"
}
