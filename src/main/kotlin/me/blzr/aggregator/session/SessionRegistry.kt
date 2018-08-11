package me.blzr.aggregator.session

import me.blzr.aggregator.exception.SessionReusedException

class SessionRegistry {
    private val sessions : MutableList<Session> = mutableListOf()

    fun addSession(session: Session) {
        if(sameWebSocketSession(session)){
            throw SessionReusedException()
        }
        sessions.add(session)
    }

    private fun sameWebSocketSession(session: Session) =
            sessions.any { it.session == session.session }
}
