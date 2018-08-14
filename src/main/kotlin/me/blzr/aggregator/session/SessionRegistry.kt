package me.blzr.aggregator.session

import me.blzr.aggregator.exception.SessionReusedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Component
class SessionRegistry {
    private val log = LoggerFactory.getLogger(SessionRegistry::class.java)
    private val watchdog = Executors.newScheduledThreadPool(WATCHDOG_POOL)
    // Primary queue of new sessions
    private val sessions = LinkedBlockingQueue<Session>()

    fun addSession(session: Session) {
        log.info("Register new session")
        if(sameWebSocketSession(session)){
            throw SessionReusedException()
        }
        sessions.offer(session)
        watchdog.schedule({
            if (session.isOpen()) {
                log.info("Session timeout")
                session.destroy()
            }
        }, TIMEOUT, TimeUnit.SECONDS)
    }

    // TODO wrap into stream
    fun getSession(): Session =
            sessions.take()

    private fun sameWebSocketSession(session: Session) =
            sessions.any { it.session == session.session }

    companion object {
        const val WATCHDOG_POOL = 5
        const val TIMEOUT = 300L // FIXME for debug purposes
    }
}
