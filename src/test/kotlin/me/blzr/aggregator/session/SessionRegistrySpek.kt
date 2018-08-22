package me.blzr.aggregator.session

import io.mockk.every
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
        every { ws1.id } returns "1"
        every { ws1.isOpen } returns true

        val ws2 = mockk<WebSocketSession>()
        every { ws2.id } returns "1"
        every { ws2.isOpen } returns true

        val message = TextMessage(
                """
                    {
                        "code": "code",
                        "brand": "foo",
                        "apikey": "bar",
                        "analog": "baz"
                    }
                    """.trimIndent())

        on("adding duplicate") {
            val session1 = Session(config, ws1, message)
            sessionRegistry.addSession(session1)

            val sessionDup = Session(config, ws1, message)
            it("should fail") {
                assertFailsWith<SessionReusedException> {
                    sessionRegistry.addSession(sessionDup)
                }
            }
        }

        on("adding new session") {
            val session2 = Session(config, ws2, message)
            it("should secceed") {
                sessionRegistry.addSession(session2)
            }
        }
    }
})
