package com.go.feature.util

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.util.exception.ValidationException

fun checkStorageForUpdateAction(applicationProperties: ApplicationProperties) {
    if (!applicationProperties.storage.enabled) {
        throw ValidationException("Operation not supported, storage disabled")
    }
}