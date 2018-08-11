package me.blzr.aggregator

import com.google.gson.Gson
import org.springframework.stereotype.Controller
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Controller
class WebSocketHandler : TextWebSocketHandler() {
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        Thread(Runnable {
            for (i in 1..3) {
                Thread.sleep(1000)
                if (session.isOpen) {
                    session.sendMessage(
                            TextMessage("Response $i: ${Gson().fromJson(message.payload, Map::class.java)}"))
                }
            }
            session.close(CloseStatus.NORMAL)
        }).start()
    }
}

