package com.grarcht.shuttle.framework.coroutines.scope

import android.content.Context
import com.grarcht.shuttle.framework.visibility.error.ShuttleDefaultError
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.cancellation.CancellationException

private const val ERROR_UNABLE_TO_CANCEL_SCOPE = "Unable to cancel the scope."

/**
 * Cancels the scope, if non-null, and catches and observes the [IllegalStateException], if thrown.
 *
 * @param scope to cancel
 * @param cause the optional cause to provide to the cancel function
 * @param context the context identifier of how the scope is used. This is not the Android [Context].
 * @param errorObservable to send errors to increase visibility into issues
 */
fun cancelScopeQuietly(
    scope: CoroutineScope?,
    cause: CancellationException? = null,
    context: String,
    errorObservable: ShuttleVisibilityObservable
) {
    scope?.let {
        try {
            scope.cancel(cause)
        } catch (e: IllegalStateException) {
            val error = ShuttleDefaultError.ObservedError(context, ERROR_UNABLE_TO_CANCEL_SCOPE, e)
            errorObservable.observe(error)
        }
    }
}
