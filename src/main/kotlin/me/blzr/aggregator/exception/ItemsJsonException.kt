package me.blzr.aggregator.exception

class ItemsJsonException : Exception() {
    override val message: String?
        get() = "E_ITEMS_JSON"
}
