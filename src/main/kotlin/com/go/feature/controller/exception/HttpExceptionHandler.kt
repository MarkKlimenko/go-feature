package com.go.feature.controller.exception

import com.go.feature.util.exception.ValidationException
import mu.KLogging
import org.springframework.cloud.sleuth.Tracer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class HttpExceptionHandler(
    val tracer: Tracer
) {

    @ExceptionHandler(ValidationException::class)
    fun validationExceptionHandler(e: ValidationException): ResponseEntity<ErrorResponse> {
        val message: String = e.message ?: "Empty message"

        logger.error("Validation exception: $message")
        return ResponseEntity(createResponse(message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun validationExceptionHandler(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Exception: ", e)
        return ResponseEntity(createResponse(e.message), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun createResponse(message: String?): ErrorResponse {
        return ErrorResponse(
            message = message,
            traceId = tracer.currentSpan()?.context()?.traceId()
        )
    }

    private companion object : KLogging()
}