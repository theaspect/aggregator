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
            val gson = Gson()
            val before = gson.toJson(mapOf("before" to gson.fromJson(message.payload, Map::class.java)))
            val after = gson.toJson(mapOf("after" to gson.fromJson(ScriptRunner.executeScript(before), Map::class.java)))

            if (session.isOpen) {
                session.sendMessage(TextMessage(after))
            }

            session.close(CloseStatus.NORMAL)
        }).start()
    }
}

