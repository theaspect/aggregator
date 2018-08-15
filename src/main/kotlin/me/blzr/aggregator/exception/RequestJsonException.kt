package me.blzr.aggregator.exception

class RequestJsonException(cause: Throwable?) : Exception(cause), AggregatorException {
    override val message: String?
        get() = "E_REQUEST_JSON"
}
