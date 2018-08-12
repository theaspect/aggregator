package me.blzr.aggregator.exception

class ScriptTimeoutException: Exception() {
    override val message: String?
        get() = "E_SCRIPT_TIMEOUT"
}
