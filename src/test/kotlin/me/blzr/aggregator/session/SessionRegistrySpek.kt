package me.blzr.aggregator.session

import io.mockk.mockk
import me.blzr.aggregator.Config
import me.blzr.aggregator.exception.SessionReusedException
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import kotlin.test.assertFailsWith

object SessionRegistrySpek : Spek({
    describe("A Registry with sessions") {
        val config = Config()
        val sessionRegistry = SessionRegistry(config)
        val ws1 = mockk<WebSocketSession>()
        val session1 = Session(config, ws1, TextMessage("Foo"))
        sessionRegistry.addSession(session1)

        on("adding duplicate") {
            val sessionDup = Session(config, ws1, TextMessage("Foo"))
            it("should fail") {
                assertFailsWith<SessionReusedException> {
                    sessionRegistry.addSession(sessionDup)
                }
            }
        }

        on("adding new session") {
            val ws2 = mockk<WebSocketSession>()
            val session2 = Session(config, ws2, TextMessage("Bar"))
            it("should secceed") {
                sessionRegistry.addSession(session2)
            }
        }
    }
})
