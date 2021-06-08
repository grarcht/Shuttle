package com.grarcht.shuttle.framework

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import com.grarcht.shuttle.framework.content.ShuttleIntent
import com.grarcht.shuttle.framework.content.bundle.BundleFactory
import com.grarcht.shuttle.framework.content.bundle.DefaultBundleFactory
import com.grarcht.shuttle.framework.content.bundle.ShuttleBundle
import com.grarcht.shuttle.framework.coroutines.channel.relayFlowIfAvailable
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.Serializable

/**
 *  This implementation of the [Shuttle] interface that is used to provide a factory that creates
 *  [ShuttleBundle] and [ShuttleIntent] cargo objects.  It is these objects which are created and access
 *  the repository to persist the data and return a small object to use in Android parcel transactions.
 *
 *  Additionally, this class is used to retrieve [ShuttleBundle] and [ShuttleIntent] cargo objects from the
 *  repository.
 *
 *  For more information on the factory design pattern, refer to:
 *  <a href="https://www.tutorialspoint.com/design_pattern/factory_pattern.htm">Factory Design Pattern</a>
 */
open class CargoShuttle(
    override val shuttleFacade: ShuttleFacade,
    override val shuttleWarehouse: ShuttleWarehouse
) : Shuttle {
    /**
     * This function creates a [ShuttleBundle].
     * @param bundle Used to create the [ShuttleBundle] object from.  For the other
     *               [bundleCargoWith] function, the bundle will be set to an empty bundle.
     * @param bundleFactory Used to create the bundle.  If the reference is null, it will be set
     *        to the [DefaultBundleFactory] by default.
     * @return The newly created [ShuttleBundle]
     */
    override fun bundleCargoWith(bundle: Bundle?, bundleFactory: BundleFactory?): ShuttleBundle {
        return ShuttleBundle.with(
            bundle,
            shuttleWarehouse,
            bundleFactory ?: DefaultBundleFactory()
        )
    }

    /**
     * This function creates a [ShuttleIntent].
     * @param intent Used by the [Intent]'s copy constructor to create a new intent
     * @return The newly created [ShuttleIntent]
     */
    override fun intentCargoWith(intent: Intent): ShuttleIntent {
        return ShuttleIntent.with(shuttleWarehouse, shuttleFacade).intent(intent)
    }

    /**
     * This function creates a [ShuttleIntent].
     * @param action The Intent action, such as ACTION_VIEW.
     * @return The newly created [ShuttleIntent]
     */
    override fun intentCargoWith(action: String): ShuttleIntent {
        return ShuttleIntent.with(shuttleWarehouse, shuttleFacade).intent(action)
    }

    /**
     * This function creates a [ShuttleIntent].
     * @param action The Intent action, such as ACTION_VIEW.
     * @param uri The Intent data URI.
     * @return The newly created [ShuttleIntent]
     */
    override fun intentCargoWith(action: String, uri: Uri): ShuttleIntent {
        return ShuttleIntent.with(shuttleWarehouse, shuttleFacade).intent(action, uri)
    }

    /**
     * This function creates a [ShuttleIntent].
     * @param packageContext A Context of the application package implementing this class.
     * @param cls The component class that is to be used for the intent.
     * @return The newly created [ShuttleIntent]
     */
    override fun intentCargoWith(packageContext: Context, cls: Class<*>): ShuttleIntent {
        return ShuttleIntent.with(shuttleWarehouse, shuttleFacade).intent(packageContext, cls)
    }

    /**
     * This function creates a [ShuttleIntent].
     * @param action Used to create a type of intent
     * @param uri The Intent data URI.
     * @param packageContext A Context of the application package implementing this class.
     * @param cls The component class that is to be used for the intent.
     * @return The newly created [ShuttleIntent]
     */
    override fun intentCargoWith(
        action: String,
        uri: Uri,
        packageContext: Context,
        cls: Class<*>
    ): ShuttleIntent {
        return ShuttleIntent.with(shuttleWarehouse, shuttleFacade).intent(action, uri, packageContext, cls)
    }

    /**
     * This function creates a [ShuttleIntent].
     * @param target The Intent that the user will be selecting an activity to perform.
     * @param title Optional title that will be displayed in the chooser, only when the target
     *              action is not ACTION_SEND or ACTION_SEND_MULTIPLE.
     * @return The Intent object that you can hand to  [Context.startActivity] and related methods.
     * @see [Intent.createChooser]
     */
    override fun intentChooserCargoWith(target: Intent, title: CharSequence?): ShuttleIntent {
        return ShuttleIntent.with(shuttleWarehouse, shuttleFacade).intentChooser(target, title)
    }

    /**
     * This function creates a [ShuttleIntent].
     * @param target The Intent that the user will be selecting an activity to perform.
     * @param title Optional title that will be displayed in the chooser, only when the target
     *              action is not ACTION_SEND or ACTION_SEND_MULTIPLE.
     * @param sender Optional IntentSender to be called when a choice is made.
     * @return The Intent object that you can hand to  [Context.startActivity] and related
     *         methods.
     * @see [Intent.createChooser]
     */
    override fun intentChooserCargoWith(
        target: Intent?,
        title: CharSequence?,
        sender: IntentSender?
    ): ShuttleIntent {
        return ShuttleIntent.with(shuttleWarehouse, shuttleFacade).intentChooser(target, title)
    }

    /**
     * Obtains the [Parcelable] cargo from the database.
     * @param cargoId used to look up the cargo in the repository
     * @return the channel with a reference to the result, a [ShuttlePickupCargoResult]
     */
    override suspend fun <D : Serializable> pickupCargo(cargoId: String): Channel<ShuttlePickupCargoResult> {
        return shuttleWarehouse.pickup<D>(cargoId)
    }

    /**
     * Cleans the Shuttle on returning to the [currentScreen] from the [nextScreenClass] via the
     * [cargoId].
     * @param currentScreen the current activity class reference
     * @param nextScreenClass the string for the name of the next screen's class
     * @param cargoId the id for the cargo shipped with Shuttle
     */
    override fun cleanShuttleOnReturnTo(
        currentScreen: Class<*>,
        nextScreenClass: Class<*>,
        cargoId: String
    ): Shuttle {
        GlobalScope.launch {
            shuttleFacade.removeCargoAfterDelivery(currentScreen, nextScreenClass, cargoId)
        }
        return this
    }

    /**
     * Removes cargo from the warehouse where the cargo matches the id.
     * @param cargoId the id for the cargo shipped with Shuttle
     */
    override fun cleanShuttleFromDeliveryFor(cargoId: String, receiver: Channel<ShuttleRemoveCargoResult>?): Shuttle {
        GlobalScope.launch {
            shuttleWarehouse.removeCargoBy(cargoId).relayFlowIfAvailable(receiver)
        }
        return this
    }

    /**
     * Performs completion events for shuttle, such as clearing the
     * database.
     */
    override fun cleanShuttleFromAllDeliveries(receiver: Channel<ShuttleRemoveCargoResult>?): Shuttle {
        GlobalScope.launch {
            shuttleWarehouse.removeAllCargo().relayFlowIfAvailable(receiver)
        }
        return this
    }
}
