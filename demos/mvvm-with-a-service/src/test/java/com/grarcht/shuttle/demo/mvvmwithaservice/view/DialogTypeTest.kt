package com.grarcht.shuttle.demo.mvvmwithaservice.view

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Verifies the functionality of [DialogType], including the integer values assigned to each
 * dialog type constant and the [DialogType.toDialogType] factory method's mapping and fallback
 * behavior.
 */
class DialogTypeTest {

    @Test
    fun verifyTypeValues() {
        assertAll(
            { assertEquals(0, DialogType.LOADING.typeValue) },
            { assertEquals(1, DialogType.CONTENT.typeValue) },
            { assertEquals(2, DialogType.ERROR.typeValue) }
        )
    }

    @Test
    fun toDialogTypeReturnsLoadingForValue0() {
        assertEquals(DialogType.LOADING, DialogType.toDialogType(0))
    }

    @Test
    fun toDialogTypeReturnsContentForValue1() {
        assertEquals(DialogType.CONTENT, DialogType.toDialogType(1))
    }

    @Test
    fun toDialogTypeReturnsErrorForValue2() {
        assertEquals(DialogType.ERROR, DialogType.toDialogType(2))
    }

    @Test
    fun toDialogTypeReturnsLoadingForUnknownValue() {
        assertEquals(DialogType.LOADING, DialogType.toDialogType(-1))
    }
}
