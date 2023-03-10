package com.go.feature


import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
class Application

fun main(args: Array<String>) {
    //runApplication<Application>(*args)

    IndexTest().go()
}
