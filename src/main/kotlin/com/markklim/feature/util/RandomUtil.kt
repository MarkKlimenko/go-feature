package com.markklim.feature.util

import java.util.UUID

fun randomId(): String = UUID.randomUUID().toString()

fun randomVersion(): String = UUID.randomUUID().toString()