package com.go.feature.util.message

import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.entity.Filter

// TODO: add error localization engine

const val NAMESPACE_NOT_FOUND_ERROR = "Namespace not found"

const val FILTER_NOT_FOUND_ERROR = "Filter not found"
const val FILTER_ALREADY_EXISTS_ERROR = "Filter already exists"

const val FEATURE_NOT_FOUND_ERROR = "Feature not found"
const val FEATURE_ALREADY_EXISTS_ERROR = "Feature already exists"

fun filterIsUsedError(filter: Filter, feature: Feature) =
    "Filter '${filter.name}' is used at most by one feature '${feature.name}'"
