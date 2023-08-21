package com.markklim.feature

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

// TODO: implement authentication (use resource library and sso test library)
// TODO: add cache
// TODO: add swagger with authorization
// TODO: check Transactional
// TODO: spring boot 3

// TODO: request body is missing - ginger library influence
fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
