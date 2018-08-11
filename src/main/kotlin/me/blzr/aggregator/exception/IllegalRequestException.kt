package me.blzr.aggregator.exception

class IllegalRequestException: Exception() {
    override val message: String?
        get() = "E_ILLEGAL_REQUEST"
}
