package com.go.feature.util

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.util.exception.client.ClientException

fun checkStorageForUpdateAction(applicationProperties: ApplicationProperties) {
    if (!applicationProperties.storage.enabled) {
        throw ClientException("Operation not supported, storage disabled")
    }
}