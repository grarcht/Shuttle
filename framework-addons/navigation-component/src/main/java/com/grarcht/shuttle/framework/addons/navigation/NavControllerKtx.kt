package com.grarcht.shuttle.framework.addons.navigation

import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.grarcht.shuttle.framework.ExcludeFromCoverage
import com.grarcht.shuttle.framework.Shuttle

/**
 * @param shuttle
 * @param resId
 */
fun NavController?.navigateWithShuttle(
    shuttle: Shuttle,
    @IdRes resId: Int
): ShuttleNavController? = navigateWithShuttleResId(shuttle, resId)

@ExcludeFromCoverage
private fun NavController?.navigateWithShuttleResId(
    shuttle: Shuttle,
    @IdRes resId: Int
): ShuttleNavController? =
    this?.let {
        ShuttleNavController.navigateWith(
            shuttle.shuttleWarehouse,
            shuttle.shuttleFacade,
            this,
            resId = resId
        )
    }

/**
 * @param shuttle
 * @param resId
 * @param navOptions
 */
fun NavController?.navigateWithShuttle(
    shuttle: Shuttle,
    @IdRes resId: Int,
    navOptions: NavOptions?
): ShuttleNavController? = navigateWithShuttleResIdOptions(shuttle, resId, navOptions)

@ExcludeFromCoverage
private fun NavController?.navigateWithShuttleResIdOptions(
    shuttle: Shuttle,
    @IdRes resId: Int,
    navOptions: NavOptions?
): ShuttleNavController? =
    this?.let {
        ShuttleNavController.navigateWith(
            shuttle.shuttleWarehouse,
            shuttle.shuttleFacade,
            this,
            resId = resId,
            navOptions = navOptions
        )
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
): ShuttleNavController? = navigateWithShuttleDirectionsOptions(shuttle, directions, navOptions)

@ExcludeFromCoverage
private fun NavController?.navigateWithShuttleDirectionsOptions(
    shuttle: Shuttle,
    directions: NavDirections,
    navOptions: NavOptions? = null
): ShuttleNavController? =
    this?.let {
        ShuttleNavController.navigateWith(
            shuttle.shuttleWarehouse,
            shuttle.shuttleFacade,
            this,
            navDirections = directions,
            navOptions = navOptions
        )
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
): ShuttleNavController? = navigateWithShuttleDirectionsExtras(shuttle, directions, navigatorExtras)

@ExcludeFromCoverage
private fun NavController?.navigateWithShuttleDirectionsExtras(
    shuttle: Shuttle,
    directions: NavDirections,
    navigatorExtras: Navigator.Extras
): ShuttleNavController? =
    this?.let {
        ShuttleNavController.navigateWith(
            shuttle.shuttleWarehouse,
            shuttle.shuttleFacade,
            this,
            navDirections = directions,
            navigatorExtras = navigatorExtras
        )
    }
