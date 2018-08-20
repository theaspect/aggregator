package me.blzr.aggregator.exception

class SessionClosedException : Exception(), AggregatorException {
    override val message: String?
        get() = "E_SESSION_CLOSED"
}
