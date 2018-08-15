package me.blzr.aggregator.session

import me.blzr.aggregator.Config
import me.blzr.aggregator.exception.SessionReusedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Component
class SessionRegistry(
        val config: Config) {
    private val log = LoggerFactory.getLogger(SessionRegistry::class.java)
    private val watchdog = Executors.newScheduledThreadPool(config.pool.watchdog)
    // Primary queue of new sessions
    private val sessions = LinkedBlockingQueue<Session>()

    fun addSession(session: Session) {
        log.debug("New $session")
        if(sameWebSocketSession(session)){
            log.error("Reused $session")
            throw SessionReusedException()
        }
        sessions.offer(session)
        watchdog.schedule({
            if (session.isOpen()) {
                log.debug("Timeout $session")
                session.destroy()
            }
        }, config.timeout.session, TimeUnit.SECONDS)
    }

    // TODO wrap into stream
    fun awaitSession(): Session = sessions.take()

    private fun sameWebSocketSession(session: Session) =
            sessions.any { it.session == session.session }
}
