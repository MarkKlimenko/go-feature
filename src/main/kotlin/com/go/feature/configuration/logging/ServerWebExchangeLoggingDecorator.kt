package ru.alfalab.starter.logging.http.reactive

import com.go.feature.configuration.logging.ServerHttpResponseLoggingDecorator
import com.go.feature.configuration.logging.properties.LoggingProperties
import com.go.feature.configuration.logging.state.RequestLoggingState
import org.springframework.core.ResolvableType
import org.springframework.core.codec.Hints
import org.springframework.http.InvalidMediaTypeException
import org.springframework.http.MediaType
import org.springframework.http.codec.HttpMessageReader
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.multipart.Part
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.util.CollectionUtils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebExchangeDecorator
import reactor.core.publisher.Mono

class ServerWebExchangeLoggingDecorator(
    delegate: ServerWebExchange,
    private val loggingProperties: LoggingProperties.HttpLoggingControlConfig,
    private val serverCodecConfigurer: ServerCodecConfigurer,
    logFieldsMap: Map<String, Any>,
    requestLoggingState: RequestLoggingState
) : ServerWebExchangeDecorator(delegate) {
    private val requestDecorator: ServerHttpRequestDecorator =
        ServerHttpRequestLoggingDecorator(delegate.request, loggingProperties)
    private val responseDecorator: ServerHttpResponseDecorator = ServerHttpResponseLoggingDecorator(
        delegate, loggingProperties, requestLoggingState, logFieldsMap
    )

    private val multipartData = lazy {
        if (loggingProperties.isExtendedLoggingEnabled) {
            computeMultipartMono().cache()
        } else {
            super.getMultipartData()
        }
    }

    override fun getRequest(): ServerHttpRequest {
        return requestDecorator
    }

    override fun getResponse(): ServerHttpResponse {
        return responseDecorator
    }

    override fun getMultipartData(): Mono<MultiValueMap<String, Part>> {
        return multipartData.value
    }

    @Suppress("unchecked_cast")
    private fun computeMultipartMono(): Mono<MultiValueMap<String, Part>> {
        val contentType = this.requestDecorator.headers.contentType
        try {
            if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
                return serverCodecConfigurer.readers.stream()
                    .filter { reader -> reader.canRead(MULTIPART_DATA_TYPE, MediaType.MULTIPART_FORM_DATA) }.findFirst()
                    .orElseThrow { IllegalStateException("No HttpMessageReader for ${MediaType.MULTIPART_FORM_DATA.type}") }
                    .let { it as HttpMessageReader<MultiValueMap<String, Part>> }
                    .run {
                        readMono(
                            MULTIPART_DATA_TYPE,
                            requestDecorator,
                            Hints.from(Hints.LOG_PREFIX_HINT, delegate.logPrefix)
                        )
                    }
            }
        } catch (_: InvalidMediaTypeException) {
            return EMPTY_MULTIPART_DATA;
        }
        return EMPTY_MULTIPART_DATA;
    }

    companion object {
        private val MULTIPART_DATA_TYPE = ResolvableType.forClassWithGenerics(
            MultiValueMap::class.java, String::class.java, Part::class.java
        )
        private val EMPTY_MULTIPART_DATA =
            Mono.just(CollectionUtils.unmodifiableMultiValueMap(LinkedMultiValueMap<String, Part>(0))).cache()

    }
}