package me.blzr.aggregator

import me.blzr.aggregator.controller.EchoWebSocketHandler
import me.blzr.aggregator.controller.WebSocketHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry


@SpringBootApplication
class AggregatorApplication

@Configuration
@EnableWebSocket
class WebSocketConfig(
        val businessLogic: BusinessLogic) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(EchoWebSocketHandler(), "/echo")
        registry.addHandler(WebSocketHandler(businessLogic), "/ws")
    }
}

/**
 * Run with -Xms32m -Xmx32m -Xss1m
 */
fun main(args: Array<String>) {
    runApplication<AggregatorApplication>(*args)
}
