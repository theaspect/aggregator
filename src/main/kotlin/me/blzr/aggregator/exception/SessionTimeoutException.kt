package me.blzr.aggregator.exception

class SessionTimeoutException: Exception(), AggregatorException {
    override val message: String?
        get() = "E_SESSION_TIMEOUT"
}
