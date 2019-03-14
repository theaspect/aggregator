package me.blzr.aggregator.controller

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Gauge
import io.prometheus.client.exporter.common.TextFormat
import me.blzr.aggregator.BusinessLogic
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletResponse


@Controller
class PrometheusController(val businessLogic: BusinessLogic) {
    private final val log = LoggerFactory.getLogger(PrometheusController::class.java)

    // We can't use counters because we use own registry
    private final val metrics: Map<String, Gauge> = listOf(
            "uptime",
            //"uptimeHuman",
            "sessionTotal",
            "sessionRegistry",
            "sessionAlive",
            "sessionFinished",
            "taskTotal",
            "taskPending",
            "taskSucceeded",
            "taskFailed"
    ).map {
        it to Gauge.Builder()
                .namespace("aggregator")
                .labelNames("node")
                .name(it)
                .help(it)
                .register()
    }.toMap()

    @RequestMapping("/prometheus")
    fun index(resp: HttpServletResponse) {
        resp.status = HttpServletResponse.SC_OK
        resp.contentType = TextFormat.CONTENT_TYPE_004

        resp.writer.use { writer ->
            val stats = businessLogic.getStats()

            metrics.forEach{k,v -> v.labels(businessLogic.config.prometheus.node).set(stats[k]?.toDouble() ?: 0.0)}

            TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples())
            writer.flush()
        }
    }
}
