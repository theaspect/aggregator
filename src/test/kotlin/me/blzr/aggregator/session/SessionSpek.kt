package me.blzr.aggregator.session

import com.google.gson.Gson
import io.mockk.mockk
import me.blzr.aggregator.Config
import me.blzr.aggregator.exception.IllegalRequestException
import me.blzr.aggregator.exception.RequestJsonException
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.springframework.web.socket.TextMessage
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

object SessionSpek : Spek({
    describe("A Session") {
        val config = Config()

        on("incorrect json") {
            val session = Session(config, mockk(), TextMessage("foo bar"))
            it("should fail") {
                assertFailsWith<RequestJsonException> {
                    session.getAllowedParams()
                }
            }
        }

        on("missing param") {
            val session = Session(config, mockk(), TextMessage(Gson().toJson(mapOf(
                    "code" to "",
                    "brand" to "",
                    "apikey" to ""
            ))))
            it("should fail") {
                assertFailsWith<IllegalRequestException> {
                    session.getAllowedParams()
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
            val session = Session(config, mockk(), TextMessage(Gson().toJson(params)))
            it("should succeed") {
                assertEquals(params, session.getAllowedParams())
            }
        }
    }
})
