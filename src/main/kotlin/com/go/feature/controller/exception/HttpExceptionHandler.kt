package com.go.feature.controller.exception

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.go.feature.util.exception.ValidationException
import mu.KLogging
import org.springframework.cloud.sleuth.Tracer
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException

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

    @ExceptionHandler(OptimisticLockingFailureException::class)
    fun optimisticLockExceptionHandler(e: OptimisticLockingFailureException): ResponseEntity<ErrorResponse> {
        logger.warn("OptimisticLockingFailure: ", e)
        return ResponseEntity(createResponse(e.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun validationExceptionHandler(e: WebExchangeBindException): ResponseEntity<ErrorResponse> {
        val validations: Map<String, String?> = e.bindingResult.fieldErrors
            .associate { Pair(it.field, it.defaultMessage) }

        logger.debug { "$VALIDATION_EXCEPTION_MESSAGE: message=${e.message}, validations=$validations" }

        return ResponseEntity(
            createResponse(VALIDATION_EXCEPTION_MESSAGE, validations),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun webInputExceptionHandler(e: ServerWebInputException): ResponseEntity<ErrorResponse> {
        val kotlinParameterException: Throwable? = e.cause?.cause

        return if (kotlinParameterException is MissingKotlinParameterException) {
            val validations: Map<String, String> = mapOf(
                (kotlinParameterException.parameter.name ?: VALIDATION_PARAMETER_NOT_FOUND)
                    to VALIDATION_NOT_NULL_MESSAGE
            )

            logger.debug { "$VALIDATION_EXCEPTION_MESSAGE: message=${e.message}, validations=$validations" }

            return ResponseEntity(
                createResponse(VALIDATION_EXCEPTION_MESSAGE, validations),
                HttpStatus.BAD_REQUEST
            )
        } else {
            logger.error("$EXCEPTION_MESSAGE: ", e)
            ResponseEntity(createResponse(e.message), HttpStatus.BAD_REQUEST)
        }
    }

    @ExceptionHandler(Exception::class)
    fun commonExceptionHandler(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("$EXCEPTION_MESSAGE: ", e)
        return ResponseEntity(createResponse(e.message), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun createResponse(message: String?, validations: Map<String, String?>? = null): ErrorResponse =
        ErrorResponse(
            message = message,
            traceId = tracer.currentSpan()?.context()?.traceId(),
            validations = validations
        )

    private companion object : KLogging() {
        private const val EXCEPTION_MESSAGE = "Exception"
        private const val VALIDATION_EXCEPTION_MESSAGE = "Validation exception"

        private const val VALIDATION_NOT_NULL_MESSAGE = "must not be null"
        private const val VALIDATION_PARAMETER_NOT_FOUND = "???"
    }
}