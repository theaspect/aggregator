package me.blzr.aggregator.task

import me.blzr.aggregator.Config
import me.blzr.aggregator.exception.SuppliersJsonException
import me.blzr.aggregator.exception.SuppliersResponseException
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

object SuppliersTaskSpek : Spek({
    describe("A Suppliers Task") {
        val config = Config()

        on("correct json") {
            it("should succeed") {
                val json = """
                {
                    "suppliers": [{
                            "class": "CManAutotrade",
                            "params": {
                                "login": "log1234",
                                "password": "1234"
                            },
                            "code": "3310",
                            "brand": "ctr",
                            "analog": "1"
                        },
                        {
                            "class": "CManRossko",
                            "params": {
                                "login": "log1234",
                                "password": "1234",
                                "domain": "msk"
                            },
                            "code": "3310",
                            "brand": "ctr",
                            "analog": "1"
                        },
                        {
                            "class": "CManArmtek",
                            "params": {
                                "apikey": "4d44cbtf14130d2fsdftpq024kd"
                            },
                            "code": "3310",
                            "brand": "ctr",
                            "analog": "1"
                        }
                    ]
                }
                """.trimIndent()
                val response = SuppliersTask(0, config, SuppliersTask.SuppliersRequest(mapOf())).parse(json)

                assertEquals(3, response.items.size)
            }
        }

        on("missed field") {
            it("should fail") {
                val json = """
                {
                    "notsuppliers": [{
                            "class": "CManAutotrade",
                            "params": {
                                "login": "log1234",
                                "password": "1234"
                            },
                            "code": "3310",
                            "brand": "ctr",
                            "analog": "1"
                        },
                        {
                            "class": "CManRossko",
                            "params": {
                                "login": "log1234",
                                "password": "1234",
                                "domain": "msk"
                            },
                            "code": "3310",
                            "brand": "ctr",
                            "analog": "1"
                        },
                        {
                            "class": "CManArmtek",
                            "params": {
                                "apikey": "4d44cbtf14130d2fsdftpq024kd"
                            },
                            "code": "3310",
                            "brand": "ctr",
                            "analog": "1"
                        }
                    ]
                }
                """.trimIndent()
                assertFailsWith<SuppliersResponseException> {
                    SuppliersTask(0, config, SuppliersTask.SuppliersRequest(mapOf())).parse(json)
                }
            }
        }

        on("incorrect json") {
            it("should fail") {
                val json = "Not a JSON"
                assertFailsWith<SuppliersJsonException> {
                    SuppliersTask(0, config, SuppliersTask.SuppliersRequest(mapOf())).parse(json)
                }
            }
        }
    }
})
