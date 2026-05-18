package com.grarcht.shuttle.framework.visibility

import com.grarcht.shuttle.framework.visibility.error.ShuttleDefaultError
import com.grarcht.shuttle.framework.visibility.information.ShuttleVisibilityFeedback
import com.grarcht.shuttle.framework.visibility.observation.ShuttleChannelVisibilityObservable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private const val CONTEXT = "TestContext"
private const val INFO_MESSAGE = "Info"

/**
 * Verifies the functionality of [ShuttleChannelVisibilityObservable]. This observable manages a
 * collection of channels through which visibility events (errors and informational messages) are
 * reported to registered observers. If it failed to forward observations or handle concurrent
 * modification safely, diagnostic events would be lost and the system would provide no feedback
 * on internal errors.
 */
class ShuttleChannelVisibilityObservableTests {

    @Test
    fun verifyAddChannelReturnsObservable() {
        val reporter = mock<ShuttleVisibilityReporter>()
        val scope = TestScope()
        val observable = ShuttleChannelVisibilityObservable(reporter, scope)
        val channel = Channel<ShuttleVisibilityData>()

        val result = observable.add(channel)

        assertAll(
            { assertNotNull(result) },
            { assertSame(observable, result) }
        )
    }

    @Test
    fun verifyObserveReportsVisibilityDataAndReturnsObservable() {
        val reporter = mock<ShuttleVisibilityReporter>()
        val scope = TestScope()
        val observable = ShuttleChannelVisibilityObservable(reporter, scope)
        val visibilityData = ShuttleVisibilityFeedback.Information<Unit>(CONTEXT, message = INFO_MESSAGE)

        val result = observable.observe(visibilityData)

        assertSame(observable, result)
        verify(reporter).reportForVisibilityWith(visibilityData)
    }

    @Test
    fun verifyDisposeClosesChannelsAndReturnsObservable() {
        val reporter = mock<ShuttleVisibilityReporter>()
        val scope = TestScope()
        val observable = ShuttleChannelVisibilityObservable(reporter, scope)
        val channel = Channel<ShuttleVisibilityData>()
        observable.add(channel)

        val result = observable.dispose()

        assertSame(observable, result)
    }

    @Test
    fun verifyObserveWithErrorCallsReporter() {
        val reporter = mock<ShuttleVisibilityReporter>()
        val scope = TestScope()
        val observable = ShuttleChannelVisibilityObservable(reporter, scope)
        val throwable = RuntimeException("test error")
        val error = ShuttleDefaultError.ObservedError(CONTEXT, "error", throwable)

        observable.observe(error)

        verify(reporter).reportForVisibilityWith(error)
    }

    @Test
    fun verifyAddHandlesConcurrentModificationException() {
        val reporter = mock<ShuttleVisibilityReporter>()
        val scope = TestScope()
        val observable = ShuttleChannelVisibilityObservable(reporter, scope)
        val cmeList = object : ArrayList<Channel<ShuttleVisibilityData>>() {
            override fun add(element: Channel<ShuttleVisibilityData>): Boolean =
                throw ConcurrentModificationException("test CME on add")
        }
        setChannelsField(observable, cmeList)

        observable.add(Channel())

        verify(reporter).reportForVisibilityWith(any())
    }

    @Test
    fun verifyDisposeHandlesConcurrentModificationExceptionOnClose() {
        val reporter = mock<ShuttleVisibilityReporter>()
        val scope = TestScope()
        val observable = ShuttleChannelVisibilityObservable(reporter, scope)
        val cmeList = object : ArrayList<Channel<ShuttleVisibilityData>>() {
            override fun iterator(): MutableIterator<Channel<ShuttleVisibilityData>> =
                throw ConcurrentModificationException("test CME on forEach")
        }
        setChannelsField(observable, cmeList)

        observable.dispose()

        verify(reporter).reportForVisibilityWith(any())
    }

    @Test
    fun verifyDisposeHandlesConcurrentModificationExceptionOnClear() {
        val reporter = mock<ShuttleVisibilityReporter>()
        val scope = TestScope()
        val observable = ShuttleChannelVisibilityObservable(reporter, scope)
        val cmeList = object : ArrayList<Channel<ShuttleVisibilityData>>() {
            override fun clear() =
                throw ConcurrentModificationException("test CME on clear")
        }
        setChannelsField(observable, cmeList)

        observable.dispose()

        verify(reporter).reportForVisibilityWith(any())
    }

    @Suppress("UNCHECKED_CAST")
    private fun setChannelsField(
        observable: ShuttleChannelVisibilityObservable,
        list: MutableList<Channel<ShuttleVisibilityData>>
    ) {
        val field = ShuttleChannelVisibilityObservable::class.java.getDeclaredField("channels")
        field.isAccessible = true
        field.set(observable, list)
    }
}
