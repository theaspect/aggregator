package me.blzr.aggregator.task

import me.blzr.aggregator.Config
import me.blzr.aggregator.exception.ItemsJsonException
import me.blzr.aggregator.exception.ItemsResponseException
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

object ItemsTaskSpek : Spek({
    describe("An Items Task") {
        val config = Config()

        on("correct json without suppliers") {
            it("should succeed") {
                val json = """
                {
                "items": [{
                        "code": "3311",
                        "brand": "ctr",
                        "name": "Товар1",
                        "price": "123",
                        "quantity": "30"
                    },
                    {
                        "code": "3302",
                        "brand": "bermo",
                        "name": "Товар2",
                        "price": "120",
                        "quantity": "10"
                    },
                    {
                        "code": "3333",
                        "brand": "kama",
                        "name": "Товар3",
                        "price": "200",
                        "quantity": "4"
                    }
                ]
            }
            """.trimIndent()
                val response = ItemsTask(0, config, "").parse(json)

                assertEquals(3, response.items.size)
                assertEquals(0, response.suppliers.size)
            }
        }

        on("correct json with suppliers") {
            it("should succeed") {
                val json = """
                {
                "items": [{
                        "code": "3311",
                        "brand": "ctr",
                        "name": "Товар1",
                        "price": "123",
                        "quantity": "30"
                    },
                    {
                        "code": "3302",
                        "brand": "bermo",
                        "name": "Товар2",
                        "price": "120",
                        "quantity": "10"
                    },
                    {
                        "code": "3333",
                        "brand": "kama",
                        "name": "Товар3",
                        "price": "200",
                        "quantity": "4"
                    }
                ],
                "suppliers": [{
                        "class": "CManRossko",
                        "params": {
                            "login": "log1234",
                            "password": "1234",
                            "domain": "msk"
                        },
                        "code": "3311",
                        "brand": "ctr",
                        "analog": "0"
                    },
                    {
                        "class": "CManRossko",
                        "params": {
                            "login": "log1234",
                            "password": "1234",
                            "domain": "msk"
                        },
                        "code": "3302",
                        "brand": "bermo",
                        "analog": "0"
                    },
                    {
                        "class": "CManRossko",
                        "params": {
                            "login": "log1234",
                            "password": "1234",
                            "domain": "msk"
                        },
                        "code": "3333",
                        "brand": "kama",
                        "analog": "0"
                    }
                ]
            }
            """.trimIndent()
                val response = ItemsTask(0, config, "").parse(json)

                assertEquals(3, response.items.size)
                assertEquals(3, response.suppliers.size)
            }
        }

        on("missed field") {
            it("should fail") {
                val json = """
                {
                    "notitems": [{
                            "code": "3311",
                            "brand": "ctr",
                            "name": "Товар1",
                            "price": "123",
                            "quantity": "30"
                        },
                        {
                            "code": "3302",
                            "brand": "bermo",
                            "name": "Товар2",
                            "price": "120",
                            "quantity": "10"
                        },
                        {
                            "code": "3333",
                            "brand": "kama",
                            "name": "Товар3",
                            "price": "200",
                            "quantity": "4"
                        }
                    ]
                }
                """.trimIndent()
                assertFailsWith<ItemsResponseException> {
                    ItemsTask(0, config, "").parse(json)
                }
            }
        }

        on("incorrect json") {
            it("should fail") {
                val json = "Not a JSON"
                assertFailsWith<ItemsJsonException> {
                    ItemsTask(0, config, "").parse(json)
                }
            }
        }
    }
})
