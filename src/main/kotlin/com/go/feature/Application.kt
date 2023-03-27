package com.go.feature

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

// TODO: add actuator + liveness + readiness

// TODO: add tests
// TODO: add test coverage
// TODO: add code analyser

// TODO: add settings engine

