package me.blzr.aggregator

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
class WebSocketConfig : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(EchoWebSocketHandler(), "/echo")
        registry.addHandler(WebSocketHandler(), "/ws")
    }
}

fun main(args: Array<String>) {
    runApplication<AggregatorApplication>(*args)
}
