package com.grarcht.shuttle.framework.addons.navigation

import android.os.Bundle
import androidx.navigation.NavDirections

/**
 * This class enables creation of new nav directions from the [arguments]
 * not created from the navigation component.
 * @param actionId from the original [NavDirections]
 * @param arguments for use with Shuttle
 */
class ShuttleNavDirections(
    override val actionId: Int,
    override val arguments: Bundle
) : NavDirections
