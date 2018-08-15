package me.blzr.aggregator.exception

class SuppliersResponseException : Exception() {
    override val message: String?
        get() = "E_SUPPLIERS_Response"
}
