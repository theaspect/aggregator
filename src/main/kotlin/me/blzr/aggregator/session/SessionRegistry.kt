package me.blzr.aggregator.session

import me.blzr.aggregator.exception.SessionReusedException
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Component
class SessionRegistry {
    private val watchdog = Executors.newScheduledThreadPool(WATCHDOG_POOL)
    // Primary queue of new sessions
    private val sessions = LinkedBlockingQueue<Session>()

    fun addSession(session: Session) {
        if(sameWebSocketSession(session)){
            throw SessionReusedException()
        }
        sessions.offer(session)
        watchdog.schedule({
            if (session.isOpen()) {
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
        const val TIMEOUT = 30L
    }
}
