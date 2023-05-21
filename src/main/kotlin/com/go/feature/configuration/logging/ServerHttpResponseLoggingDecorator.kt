package com.go.feature.configuration.logging

import com.go.feature.configuration.logging.properties.BODY
import com.go.feature.configuration.logging.properties.EMPTY_VALUE
import com.go.feature.configuration.logging.properties.LoggingProperties
import com.go.feature.configuration.logging.properties.RESPONSE_CODE
import com.go.feature.configuration.logging.properties.RESPONSE_INFO_TAG
import com.go.feature.configuration.logging.properties.RESPONSE_TIME
import com.go.feature.configuration.logging.state.RequestLoggingState
import com.go.feature.configuration.logging.utils.getBodyString
import com.go.feature.configuration.logging.utils.isBinaryContent
import com.go.feature.configuration.logging.utils.isNotEmpty
import com.go.feature.configuration.logging.utils.log
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.util.context.ContextView

class ServerHttpResponseLoggingDecorator(
    exchange: ServerWebExchange,
    private val loggingProperties: LoggingProperties.HttpLoggingControlConfig,
    private val requestLoggingState: RequestLoggingState,
    private val logFieldsMap: Map<String, Any>
) : ServerHttpResponseDecorator(exchange.response) {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @SuppressWarnings("kotlin:S1192")
    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        return if (log.isInfoEnabled || (log.isErrorEnabled && delegate.statusCode?.isError == true)) {
            if (isExtendedLoggingEnabled()) {
                super.writeWith(DataBufferUtils.join(body)
                    .transformDeferredContextual { dataBufferMono: Mono<DataBuffer?>, _: ContextView? ->
                        dataBufferMono
                            .doOnNext {
                                logResponseBody(it)
                            }
                    }
                )
            } else {
                logResponseBody(null)
                super.writeWith(body.toFlux())
            }

        } else {
            requestLoggingState.responseLogged = true
            super.writeWith(body.toFlux())
        }
    }

    private fun logResponseBody(dataBuffer: DataBuffer?) {
        val logFieldsResponseMap: MutableMap<String, Any> = logFieldsMap.toMutableMap()
        logFieldsResponseMap[RESPONSE_CODE] = delegate.statusCode?.value() ?: EMPTY_VALUE
        logFieldsResponseMap[RESPONSE_TIME] = requestLoggingState.timeSpent()

        if (dataBuffer != null && dataBuffer.isNotEmpty()) {
            logFieldsResponseMap[BODY] = getBodyString(
                dataBuffer,
                loggingProperties
            )
        }

        if (delegate.statusCode?.is4xxClientError == true) {
            log.log(loggingProperties.clientErrorsLevel, RESPONSE_INFO_TAG, logFieldsResponseMap)
        } else if (delegate.statusCode?.isError == true) {
            log.error(RESPONSE_INFO_TAG, logFieldsResponseMap)
        } else {
            log.info(RESPONSE_INFO_TAG, logFieldsResponseMap)
        }
        requestLoggingState.responseLogged = true
    }

    private fun isExtendedLoggingEnabled() = loggingProperties.isExtendedLoggingEnabled
        && (!delegate.isBinaryContent() || loggingProperties.isBinaryContentLoggingEnabled)
}