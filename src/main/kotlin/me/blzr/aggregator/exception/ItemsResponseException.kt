package me.blzr.aggregator.exception

class ItemsResponseException : Exception(), AggregatorException {
    override val message: String?
        get() = "E_ITEMS_RESPONSE"
}
