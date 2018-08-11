package me.blzr.aggregator.exception

class SessionReusedException : Exception() {
    override val message: String?
        get() = "E_SESSION_REUSED"
}
