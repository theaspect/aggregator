package me.blzr.aggregator

import org.springframework.stereotype.Controller
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Controller
class WebSocketHandler(
        val businessLogic: BusinessLogic) : TextWebSocketHandler() {

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        businessLogic.newSession(session, message)
    }
}
