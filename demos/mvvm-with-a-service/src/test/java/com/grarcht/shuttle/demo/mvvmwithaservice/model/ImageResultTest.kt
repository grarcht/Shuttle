package com.grarcht.shuttle.demo.mvvmwithaservice.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

/**
 * Verifies the functionality of [ImageResult], including the integer state values assigned to
 * each result constant and the [ImageResult.getImageResult] factory method's mapping and fallback
 * behavior for unknown states.
 */
class ImageResultTest {

    @Test
    fun verifyStateValues() {
        assertAll(
            { assertEquals(0, ImageResult.UNKNOWN.state) },
            { assertEquals(1, ImageResult.LOADING.state) },
            { assertEquals(3, ImageResult.SUCCESS.state) },
            { assertEquals(4, ImageResult.ERROR.state) }
        )
    }

    @Test
    fun getImageResultReturnsUnknownForState0() {
        assertEquals(ImageResult.UNKNOWN, ImageResult.getImageResult(0))
    }

    @Test
    fun getImageResultReturnsLoadingForState1() {
        assertEquals(ImageResult.LOADING, ImageResult.getImageResult(1))
    }

    @Test
    fun getImageResultReturnsSuccessForState3() {
        assertEquals(ImageResult.SUCCESS, ImageResult.getImageResult(3))
    }

    @Test
    fun getImageResultReturnsErrorForState4() {
        assertEquals(ImageResult.ERROR, ImageResult.getImageResult(4))
    }

    @Test
    fun getImageResultReturnsUnknownForUnmappedState() {
        assertEquals(ImageResult.UNKNOWN, ImageResult.getImageResult(-1))
    }
}
