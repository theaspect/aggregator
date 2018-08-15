package me.blzr.aggregator.exception

class SuppliersResponseException : Exception(), AggregatorException {
    override val message: String?
        get() = "E_SUPPLIERS_RESPONSE"
}
