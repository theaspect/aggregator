package me.blzr.aggregator

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThread(private val name: String) : ThreadFactory {
    private val i = AtomicInteger(1)
    override fun newThread(r: Runnable?): Thread {
        val t = Thread(r)
        t.name = "$name-${i.incrementAndGet()}"
        return t
    }
}
