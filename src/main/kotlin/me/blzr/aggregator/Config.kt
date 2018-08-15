package me.blzr.aggregator

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("aggregator")
class Config {
    val script = Script()
    val timeout = Timeout()
    val pool = Pool()
    val fields = Fields()

    class Script {
        lateinit var items: String
        lateinit var suppliers: String
    }

    class Timeout {
        var script: Long = 5
        var session: Long = 30
    }

    class Pool {
        var executor: Int = 20
        var watchdog: Int = 5
    }

    class Fields {
        lateinit var request: List<String>
        lateinit var items: String
        lateinit var suppliers: String
        lateinit var faulty: List<String>
    }
}
