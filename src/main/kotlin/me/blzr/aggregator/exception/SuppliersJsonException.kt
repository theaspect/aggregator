package me.blzr.aggregator.exception

class SuppliersJsonException : Exception(), AggregatorException {
    override val message: String?
        get() = "E_SUPPLIERS_JSON"
}
