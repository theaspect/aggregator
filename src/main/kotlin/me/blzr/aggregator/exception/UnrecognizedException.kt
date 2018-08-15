package me.blzr.aggregator.exception

class UnrecognizedException : Exception(), AggregatorException {
    override val message: String?
        get() = "E_UNKNOWN"
}
