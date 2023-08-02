package com.markklim.feature.test.utils

import org.junit.jupiter.api.Assertions
import org.springframework.boot.test.system.CapturedOutput

fun CapturedOutput.assertContains(value: String) {
    Assertions.assertTrue(
        this.out.contains(value, true)
    )
}

fun CapturedOutput.assertNotContains(value: String) {
    Assertions.assertFalse(
        this.out.contains(value, true)
    )
}