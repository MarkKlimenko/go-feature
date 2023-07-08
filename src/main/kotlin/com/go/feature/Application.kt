package com.go.feature

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

// TODO: implement authentication (use resource library and sso test library)
// TODO: add cache
// TODO: add swagger with authorization
fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
