package me.blzr.aggregator.exception

class ItemsResponseException : Exception() {
    override val message: String?
        get() = "E_ITEMS_RESPONSE"
}
