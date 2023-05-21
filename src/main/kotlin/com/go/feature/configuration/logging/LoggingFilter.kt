package com.go.feature.configuration.logging

import com.go.feature.configuration.logging.extractor.ReactiveParametersExtractor
import com.go.feature.configuration.logging.masking.ParametersMasker
import com.go.feature.configuration.logging.properties.BODY
import com.go.feature.configuration.logging.properties.BODY_MULTIPART
import com.go.feature.configuration.logging.properties.EMPTY_VALUE
import com.go.feature.configuration.logging.properties.LoggingProperties
import com.go.feature.configuration.logging.properties.REQUEST_INFO_TAG
import com.go.feature.configuration.logging.properties.REQUEST_METHOD
import com.go.feature.configuration.logging.properties.REQUEST_URI
import com.go.feature.configuration.logging.properties.RESPONSE_CODE
import com.go.feature.configuration.logging.properties.RESPONSE_INFO_TAG
import com.go.feature.configuration.logging.properties.RESPONSE_TIME
import com.go.feature.configuration.logging.state.RequestLoggingState
import com.go.feature.configuration.logging.utils.formattedBody
import com.go.feature.configuration.logging.utils.getBodyString
import com.go.feature.configuration.logging.utils.isBinaryContent
import com.go.feature.configuration.logging.utils.isMultipart
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import ru.alfalab.starter.logging.http.reactive.ServerWebExchangeLoggingDecorator

class LoggingFilter(
    private val loggingProperties: LoggingProperties,
    private val requestParamsExtractor: ReactiveParametersExtractor,
    private val parametersMasker: ParametersMasker,
    private val serverCodecConfigurer: ServerCodecConfigurer
) : WebFilter {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val requestUri = request.path.pathWithinApplication().value()

        return when (isLoggingAllowed()) {
            true -> logRequestResponse(request, requestUri, chain, exchange)
            false -> chain.filter(exchange)
        }
    }

    private fun isLoggingAllowed(): Boolean {
        // TODO: request: ServerHttpRequest
        // loggingProperties.http.isUriAllowedForLogging(request.path.pathWithinApplication().value())
        val isUriAllowedForLogging = true

        // TODO: val contentType = request.headers.getFirst(HttpHeaders.CONTENT_TYPE)
        // loggingProperties.http.isContentTypeAllowedForLogging(contentType)
        val isContentTypeAllowedForLogging = true

        return log.isInfoEnabled && isUriAllowedForLogging && isContentTypeAllowedForLogging
    }

    private fun logRequestResponse(
        request: ServerHttpRequest,
        requestUri: String,
        chain: WebFilterChain,
        exchange: ServerWebExchange
    ): Mono<Void> {
        val logFieldsMap: Map<String, Any> = createLogFieldsMap(request, requestUri)
        val webFluxProperties = loggingProperties.http.webFlux
        val requestLoggingState = RequestLoggingState()
        requestLoggingState.startTime = requestLoggingState.clock.millis()
        val decoratedExchange = ServerWebExchangeLoggingDecorator(
            exchange,
            webFluxProperties,
            serverCodecConfigurer,
            logFieldsMap,
            requestLoggingState
        )
        exchange.attributes[loggingProperties.http.webFlux.decoratedExchangeAttributeName] = decoratedExchange

        return logRequestBody(decoratedExchange, logFieldsMap)
            .then(chain.filter(decoratedExchange)
                .doAfterTerminate {
                    logResponseFinally(requestLoggingState, logFieldsMap, decoratedExchange)
                }
            )
    }

    private fun logRequestBody(
        decorator: ServerWebExchangeLoggingDecorator,
        logFieldsMap: Map<String, Any>,
    ): Mono<Any> {
        val logFieldsRequestMap: MutableMap<String, Any> = logFieldsMap.toMutableMap()
        val webFluxProps = loggingProperties.http.webFlux

        if (webFluxProps.isExtendedLoggingEnabled
            && log.isInfoEnabled
            && (!decorator.request.isBinaryContent() || webFluxProps.isBinaryContentLoggingEnabled)
        ) {
            return if (decorator.request.isMultipart()) {
                decorator.multipartData.flatMap { multiPartData ->
                    multiPartData.formattedBody(webFluxProps.isBinaryContentLoggingEnabled)
                        .doOnNext {
                            logFieldsRequestMap[BODY_MULTIPART] = it

                            log.info(REQUEST_INFO_TAG, logFieldsRequestMap)
                        }
                }.switchIfEmpty(Mono.defer {
                    log.info(REQUEST_INFO_TAG, logFieldsRequestMap)
                    Mono.empty()
                }).then(Mono.empty())
            } else {
                decorator.request.body
                    .doOnNext {
                        logFieldsRequestMap[BODY] = getBodyString(
                            it,
                            loggingProperties.http.webFlux
                        )

                        log.info(REQUEST_INFO_TAG, logFieldsRequestMap)
                    }
                    .switchIfEmpty(Mono.defer {
                        log.info(REQUEST_INFO_TAG, logFieldsRequestMap)
                        Mono.empty()
                    })
                    .then(Mono.empty())
            }
        }

        log.info("Server request: $logFieldsMap")
        return Mono.empty()
    }

    private fun logResponseFinally(
        requestLoggingState: RequestLoggingState,
        logFieldsMap: Map<String, Any>,
        exchange: ServerWebExchange
    ) {
        if (!requestLoggingState.responseLogged) {
            val logFieldsResponseMap: MutableMap<String, Any> = logFieldsMap.toMutableMap()

            logFieldsResponseMap[RESPONSE_CODE] = exchange.response.statusCode?.value() ?: EMPTY_VALUE
            logFieldsResponseMap[RESPONSE_TIME] = requestLoggingState.timeSpent()

            if (exchange.response.statusCode?.isError == true) {
                log.error(RESPONSE_INFO_TAG, logFieldsResponseMap)
            } else {
                log.info(RESPONSE_INFO_TAG, logFieldsResponseMap)
            }
        }
    }

    private fun createLogFieldsMap(request: ServerHttpRequest, requestUri: String): Map<String, Any> {
        val logFieldsMap = mutableMapOf<String, Any>()
        logFieldsMap[REQUEST_METHOD] = request.method?.toString() ?: EMPTY_VALUE
        logFieldsMap[REQUEST_URI] = requestUri
        logFieldsMap.putAll(
            if (loggingProperties.http.loggedHeaders.isEmpty() && loggingProperties.http.excludedHeaders.isEmpty()) {
                requestParamsExtractor.extractAllHeaders(request)
            } else {
                requestParamsExtractor.extractSpecificHeaders(
                    request,
                    loggingProperties.http.loggedHeaders,
                    loggingProperties.http.excludedHeaders
                )
            }
        )
        logFieldsMap.putAll(parametersMasker.maskParametersReactive(logFieldsMap, loggingProperties.http.maskedHeaders))
        logFieldsMap.putAll(
            if (loggingProperties.http.loggedQueryParams.isEmpty()) requestParamsExtractor.extractAllQueries(request)
            else requestParamsExtractor.extractSpecificQueries(request, loggingProperties.http.loggedQueryParams)
        )
        return logFieldsMap
    }
}