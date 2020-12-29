package com.grarcht.shuttle.framework.addons.navigation

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavDirections

/**
 * This class enables creation of new nav directions from the [arguments]
 * not created from the navigation component.
 * @param actionId from the original [NavDirections]
 * @param arguments for use with Shuttle
 */
class ShuttleNavDirections(
    private val actionId: Int,
    private val arguments: Bundle
) : NavDirections {
    /**
     * Returns a action id to navigate with.
     *
     * @return id of an action
     */
    @IdRes
    override fun getActionId(): Int {
        return actionId
    }

    /**
     * Returns arguments to pass to the destination
     */
    override fun getArguments(): Bundle {
        return arguments
    }
}
