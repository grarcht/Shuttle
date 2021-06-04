package com.grarcht.shuttle.framework.screen

import android.app.Application
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.grarcht.shuttle.framework.warehouse.ShuttleDataWarehouse
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.spy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ShuttleCargoFacadeTests {
    private class TestActivity : AppCompatActivity()

    @Test
    fun verifyCargoIsRemovedAfterDelivery() {
        // Given
        val countDownLatch = CountDownLatch(1)
        val application = mock(Application::class.java)
        val warehouse = ShuttleDataWarehouse()
        val handler = mock(Handler::class.java)
        val facade = spy(ShuttleCargoFacade(application, warehouse, handler))
        val screenCallback = spy(facade.screenCallback)
        val cargoId = "cargoId1"
        val firstScreenClass = TestActivity().javaClass
        val nextScreenClass = TestActivity().javaClass
        val activity = spy(TestActivity())

        // When
        doAnswer {
            val runnable = it.getArgument(0, Runnable::class.java)
            runnable?.run()
            true
        }.`when`(handler).post(any())
        facade.removeCargoAfterDelivery(firstScreenClass, nextScreenClass, cargoId)
        screenCallback.onActivityCreated(activity)
        activity.onBackPressed()
        countDownLatch.await(1, TimeUnit.SECONDS)

        // Verify
        verify(screenCallback).onActivityCreated(activity)
        verify(activity, times(2)).onBackPressed()
        Assertions.assertEquals(1, warehouse.numberOfRemoveInvocations)
    }
}
