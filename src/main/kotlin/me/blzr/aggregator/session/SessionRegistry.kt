package me.blzr.aggregator.session

import me.blzr.aggregator.exception.SessionReusedException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SessionRegistry {
    private val watchdog = Executors.newScheduledThreadPool(WATCHDOG_POOL)
    private val sessions : MutableList<Session> = mutableListOf()

    fun addSession(session: Session) {
        if(sameWebSocketSession(session)){
            throw SessionReusedException()
        }
        sessions.add(session)
        watchdog.schedule({
            if (session.isAlive()) {
                session.destroy()
            }
        }, TIMEOUT, TimeUnit.SECONDS)
    }

    private fun sameWebSocketSession(session: Session) =
            sessions.any { it.session == session.session }

    companion object {
        const val WATCHDOG_POOL = 5
        const val TIMEOUT = 30L
    }
}
