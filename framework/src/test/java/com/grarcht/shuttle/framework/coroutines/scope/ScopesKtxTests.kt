package com.grarcht.shuttle.framework.coroutines.scope

import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.coroutines.EmptyCoroutineContext

private const val SCOPE_CONTEXT = "test context"

/**
 * Verifies the functionality of [cancelScopeQuietly]. This extension function safely cancels a
 * CoroutineScope and reports any errors through the visibility observable, preventing unhandled
 * exceptions from propagating. If it behaved incorrectly, coroutine scopes could leak or errors
 * could go unreported during shutdown paths.
 */
class ScopesKtxTests {

    @Test
    fun verifyCancelScopeQuietlyDoesNothingWhenScopeIsNull() {
        val errorObservable = mock<ShuttleVisibilityObservable>()

        cancelScopeQuietly(scope = null, context = SCOPE_CONTEXT, errorObservable = errorObservable)

        verify(errorObservable, never()).observe(any())
    }

    @Test
    fun verifyCancelScopeQuietlyCancelsNonNullScope() {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())

        cancelScopeQuietly(scope = scope, context = SCOPE_CONTEXT, errorObservable = errorObservable)

        assertFalse(scope.isActive)
    }

    @Test
    fun verifyCancelScopeQuietlyObservesErrorOnIllegalStateException() {
        val errorObservable = mock<ShuttleVisibilityObservable>()
        whenever(errorObservable.observe(any())).thenReturn(errorObservable)
        // A scope with no Job causes cancel() to throw IllegalStateException
        val scope = object : CoroutineScope {
            override val coroutineContext = EmptyCoroutineContext
        }

        cancelScopeQuietly(scope = scope, context = SCOPE_CONTEXT, errorObservable = errorObservable)

        verify(errorObservable).observe(any())
    }
}
