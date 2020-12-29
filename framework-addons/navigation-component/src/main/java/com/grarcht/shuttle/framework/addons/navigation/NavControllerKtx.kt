package com.grarcht.shuttle.framework.addons.navigation

import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.grarcht.shuttle.framework.Shuttle

/**
 * @param shuttle
 * @param resId
 */
fun NavController?.navigateWithShuttle(
    shuttle: Shuttle,
    @IdRes resId: Int
): ShuttleNavController? {
    return this?.let {
        ShuttleNavController.navigateWith(
            shuttle.shuttleWarehouse,
            shuttle.shuttleFacade,
            this,
            resId = resId
        )
    }
}

/**
 * @param shuttle
 * @param resId
 * @param navOptions
 */
fun NavController?.navigateWithShuttle(
    shuttle: Shuttle,
    @IdRes resId: Int,
    navOptions: NavOptions? = null
): ShuttleNavController? {
    return this?.let {
        ShuttleNavController.navigateWith(
            shuttle.shuttleWarehouse,
            shuttle.shuttleFacade,
            this,
            resId = resId,
            navOptions = navOptions
        )
    }
}

/**
 * @param shuttle
 * @param directions
 * @param navOptions
 */
fun NavController?.navigateWithShuttle(
    shuttle: Shuttle,
    directions: NavDirections,
    navOptions: NavOptions? = null
): ShuttleNavController? {
    return this?.let {
        ShuttleNavController.navigateWith(
            shuttle.shuttleWarehouse,
            shuttle.shuttleFacade,
            this,
            navDirections = directions,
            navOptions = navOptions
        )
    }
}

/**
 * @param shuttle
 * @param directions
 * @param navigatorExtras
 */
fun NavController?.navigateWithShuttle(
    shuttle: Shuttle,
    directions: NavDirections,
    navigatorExtras: Navigator.Extras
): ShuttleNavController? {
    return this?.let {
        ShuttleNavController.navigateWith(
            shuttle.shuttleWarehouse,
            shuttle.shuttleFacade,
            this,
            navDirections = directions,
            navigatorExtras = navigatorExtras
        )
    }
}
