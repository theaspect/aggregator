package me.blzr.aggregator.controller

import me.blzr.aggregator.BusinessLogic
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody


@Controller
class StatsController(val businessLogic: BusinessLogic) {
    private final val log = LoggerFactory.getLogger(StatsController::class.java)

    @RequestMapping("/stats")
    @ResponseBody
    fun index(): Map<String, Any> {
        return businessLogic.getStats()
    }
}
