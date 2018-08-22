package me.blzr.aggregator.session

import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import me.blzr.aggregator.Config
import me.blzr.aggregator.exception.IllegalRequestException
import me.blzr.aggregator.exception.RequestJsonException
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.fail

object SessionSpek : Spek({
    describe("A Session") {
        val config = Config()

        val ws = mockk<WebSocketSession>()
        every { ws.id } returns "1"
        every { ws.isOpen } returns true

        on("incorrect json") {
            val session = Session(config, ws, TextMessage("foo bar"))
            it("should fail") {
                assertFalse { session.isAlive() }
                try{
                    session.completableFuture.get()
                    fail("Should throw exception")
                }catch (e: Exception){
                    assertEquals(RequestJsonException::class.java, e.cause!!::class.java)
                }
            }
        }

        on("missing param") {
            val session = Session(config, ws, TextMessage(Gson().toJson(mapOf(
                    "code" to "",
                    "brand" to "",
                    "apikey" to ""
            ))))
            it("should fail") {
                assertFalse { session.isAlive() }
                try{
                    session.completableFuture.get()
                    fail("Should throw exception")
                }catch (e: Exception){
                    assertEquals(IllegalRequestException::class.java, e.cause!!::class.java)
                }
            }
        }

        on("correct params") {
            val params = mapOf(
                    "code" to "1",
                    "brand" to "2",
                    "apikey" to "3",
                    "analog" to "4"
            )
            val session = Session(config, ws, TextMessage(Gson().toJson(params)))
            it("should succeed") {
                assertEquals(params, session.params)
            }
        }
    }
})
