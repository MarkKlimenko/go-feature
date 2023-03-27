package com.go.feature.controller.exception

import com.go.feature.util.exception.ValidationException
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class HttpExceptionHandler {

    @ExceptionHandler(ValidationException::class)
    fun validationExceptionHandler(e: ValidationException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = e.message ?: ""
        )

        logger.error("Validation exception: ${e.message}")
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    private companion object : KLogging()
}