package me.blzr.aggregator.exception

class SessionReusedException : Exception(), AggregatorException {
    override val message: String?
        get() = "E_SESSION_REUSED"
}
