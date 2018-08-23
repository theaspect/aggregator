package me.blzr.aggregator.session

import me.blzr.aggregator.Config
import me.blzr.aggregator.NamedThread
import me.blzr.aggregator.exception.SessionClosedException
import me.blzr.aggregator.exception.SessionReusedException
import me.blzr.aggregator.exception.SessionTimeoutException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Component
class SessionRegistry(
        val config: Config) {
    private val log = LoggerFactory.getLogger(SessionRegistry::class.java)
    private val watchdog = Executors.newScheduledThreadPool(config.pool.watchdog, NamedThread("session-watchdog"))
    // Primary queue of new sessions
    private val sessions = LinkedBlockingQueue<Session>()
    private val sessionMap = WeakHashMap<WebSocketSession, Session>()

    fun addSession(session: Session): Boolean {
        sessionMap[session.session] = session

        if (!session.isAlive()) {
            log.warn("Session already dead: $session")
            return false
        }

        log.info("New $session")
        if(sameWebSocketSession(session)){
            log.error("Reused $session")
            throw SessionReusedException()
        }

        sessions.offer(session)
        watchdog.schedule({
            if (session.isOpen()) {
                log.debug("Timeout $session")
                session.fail(SessionTimeoutException())
            }
        }, config.timeout.session, TimeUnit.SECONDS)
        return true
    }

    // TODO wrap into stream
    fun awaitSession(): Session = sessions.take()

    private fun sameWebSocketSession(session: Session) =
            sessions.any { it.session == session.session }

    fun close(session: WebSocketSession) {
        log.debug("Session ${session.id} closed by browser")
        sessionMap[session]?.fail(SessionClosedException())
    }

    fun pending() = sessions.size.toLong()
    fun alive() = sessionMap.values.count { it.isAlive() }
    fun tasks() = sessionMap.values.map { it.tasks() }.sum()
}
