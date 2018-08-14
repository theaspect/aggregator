package me.blzr.aggregator

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Controller
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Controller
class EchoWebSocketHandler : TextWebSocketHandler() {
    private final val log = LoggerFactory.getLogger(EchoWebSocketHandler::class.java)
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {

        Thread(Runnable {
            val gson = Gson()
            val before = gson.toJson(mapOf("before" to gson.fromJson(message.payload, Map::class.java)))
            val after = gson.toJson(mapOf("after" to gson.fromJson(executeScript(before), Map::class.java)))

            for(i in 0..3) {
                if (session.isOpen) {
                    Thread.sleep(1000)
                    session.sendMessage(TextMessage(after))
                }
            }

            session.close(CloseStatus.NORMAL)
        }).start()
    }

    private fun loadScript(path: String) =
            ClassPathResource(path).inputStream.bufferedReader().readText()

    private fun executeScript(stdin: String): String {
        val process = ProcessBuilder("php", "-r", loadScript("/script.php")).start()
        val writer = process.outputStream.writer()
        writer.write(stdin)
        writer.close()
        return process.inputStream.bufferedReader().readText()
    }
}

