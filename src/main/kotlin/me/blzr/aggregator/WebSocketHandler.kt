package me.blzr.aggregator

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Controller
class WebSocketHandler(
        val businessLogic: BusinessLogic) : TextWebSocketHandler() {
    private final val log = LoggerFactory.getLogger(WebSocketHandler::class.java)

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        log.info("New session ${session.id}")
        businessLogic.newSession(session, message)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        businessLogic.closeSession(session, status)
    }
}
