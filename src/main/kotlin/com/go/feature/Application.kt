package com.go.feature

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

// TODO: implement authentication
fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
