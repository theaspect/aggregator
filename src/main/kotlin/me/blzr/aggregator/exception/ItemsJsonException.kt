package me.blzr.aggregator.exception

class ItemsJsonException : Exception(), AggregatorException {
    override val message: String?
        get() = "E_ITEMS_JSON"
}
