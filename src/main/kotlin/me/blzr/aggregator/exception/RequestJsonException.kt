package me.blzr.aggregator.exception

class RequestJsonException(cause: Throwable?) : Exception(cause) {
    override val message: String?
        get() = "E_REQUEST_JSON"
}
