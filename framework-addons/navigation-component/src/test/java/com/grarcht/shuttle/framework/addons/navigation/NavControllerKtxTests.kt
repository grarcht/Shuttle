package com.grarcht.shuttle.framework.addons.navigation

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.grarcht.shuttle.framework.CargoShuttle
import com.grarcht.shuttle.framework.addons.ArchtTestTaskExecutorExtension
import com.grarcht.shuttle.framework.addons.warehouse.ShuttleDataWarehouse
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock

private const val KTX_ACTION_ID = 6000
private val KTX_ARGUMENTS = Bundle()

/**
 * Verifies the functionality of [navigateWithShuttle]. These NavController extension functions
 * integrate Shuttle with the Jetpack Navigation component, returning a ShuttleNavController that
 * can transport cargo as part of a navigation action. Without them, consumers would have no
 * concise API to combine navigation and cargo transport in a single call chain.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ArchtTestTaskExecutorExtension::class)
class NavControllerKtxTests {

    @Test
    fun verifyNavigateWithShuttleResId() {
        val navController = mock(NavController::class.java)
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val cargoShuttle = CargoShuttle(shuttleFacade, ShuttleDataWarehouse())
        val resId = androidx.navigation.fragment.R.id.nav_host_fragment_container

        val result = navController.navigateWithShuttle(cargoShuttle, resId)
        Assertions.assertNotNull(result)
    }

    @Test
    fun verifyNavigateWithShuttleResIdAndNavOptions() {
        val navController = mock(NavController::class.java)
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val cargoShuttle = CargoShuttle(shuttleFacade, ShuttleDataWarehouse())
        val resId = androidx.navigation.fragment.R.id.nav_host_fragment_container
        val navOptions = NavOptions.Builder().build()

        val result = navController.navigateWithShuttle(cargoShuttle, resId, navOptions)
        Assertions.assertNotNull(result)
    }

    @Test
    fun verifyNavigateWithShuttleResIdAndNullNavOptions() {
        val navController = mock(NavController::class.java)
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val cargoShuttle = CargoShuttle(shuttleFacade, ShuttleDataWarehouse())
        val resId = androidx.navigation.fragment.R.id.nav_host_fragment_container

        // Call with explicit null navOptions to cover the default parameter path
        val result = navController.navigateWithShuttle(cargoShuttle, resId, navOptions = null)
        Assertions.assertNotNull(result)
    }

    @Test
    fun verifyNavigateWithShuttleDirectionsAndNavOptions() {
        val navController = mock(NavController::class.java)
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val cargoShuttle = CargoShuttle(shuttleFacade, ShuttleDataWarehouse())
        val directions = TestNavDirections()
        val navOptions = NavOptions.Builder().build()

        val result = navController.navigateWithShuttle(cargoShuttle, directions, navOptions)
        Assertions.assertNotNull(result)
    }

    @Test
    fun verifyNavigateWithShuttleDirectionsAndDefaultNavOptions() {
        val navController = mock(NavController::class.java)
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val cargoShuttle = CargoShuttle(shuttleFacade, ShuttleDataWarehouse())
        val directions = TestNavDirections()

        // Call with default navOptions to cover the default parameter path
        val result = navController.navigateWithShuttle(cargoShuttle, directions)
        Assertions.assertNotNull(result)
    }

    @Test
    fun verifyNavigateWithShuttleDirectionsAndNavigatorExtras() {
        val navController = mock(NavController::class.java)
        val shuttleFacade = mock(ShuttleFacade::class.java)
        val cargoShuttle = CargoShuttle(shuttleFacade, ShuttleDataWarehouse())
        val directions = TestNavDirections()
        val navigatorExtras = mock(Navigator.Extras::class.java)

        val result = navController.navigateWithShuttle(cargoShuttle, directions, navigatorExtras)
        Assertions.assertNotNull(result)
    }

    private class TestNavDirections(
        override val actionId: Int = KTX_ACTION_ID,
        override val arguments: Bundle = KTX_ARGUMENTS
    ) : NavDirections
}
