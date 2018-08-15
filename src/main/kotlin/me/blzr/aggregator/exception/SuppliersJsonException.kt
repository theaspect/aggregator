package me.blzr.aggregator.exception

class SuppliersJsonException : Exception() {
    override val message: String?
        get() = "E_SUPPLIERS_JSON"
}
