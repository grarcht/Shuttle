package com.grarcht.shuttle.framework.screen

import android.app.Application
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.grarcht.shuttle.framework.ArchtTestTaskExecutorExtension
import com.grarcht.shuttle.framework.warehouse.ShuttleDataWarehouse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@ExtendWith(ArchtTestTaskExecutorExtension::class)
class ShuttleCargoFacadeTests {

    @Test
    fun verifyCargoIsRemovedAfterDelivery() {
        val application = mock(Application::class.java)
        val warehouse = ShuttleDataWarehouse()
        val handler = mock(Handler::class.java)
        val facade = spy(ShuttleCargoFacade(application, warehouse, handler))
        val screenCallback = spy(facade.screenCallback)
        val cargoId = "cargoId1"
        val firstScreenClass = TestActivity::class.java
        val nextScreenClass = TestActivity::class.java
        val activity = spy(TestActivity())

        doAnswer {
            val runnable = it.getArgument(0, Runnable::class.java)
            runnable?.run()
            true
        }.`when`(handler).post(any())
        facade.removeCargoAfterDelivery(firstScreenClass, nextScreenClass, cargoId)
        screenCallback.onActivityCreated(activity)
        activity.onBackPressedDispatcher.onBackPressed()

        CountDownLatch(1).await(1, TimeUnit.SECONDS)

        verify(screenCallback).onActivityCreated(activity)
        Assertions.assertEquals(1, warehouse.numberOfRemoveInvocations)
    }

    private class TestActivity : AppCompatActivity()
}
