package com.go.feature.configuration.logging.state

import java.time.Clock

class RequestLoggingState(
    var responseLogged: Boolean = false,
    val clock: Clock = Clock.systemUTC(),
    var startTime: Long = clock.millis()
) {
    fun timeSpent(): Long {
        return clock.millis() - startTime
    }
}