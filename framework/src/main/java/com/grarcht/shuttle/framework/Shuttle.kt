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
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import kotlinx.coroutines.channels.Channel
import java.io.Serializable

/**
 *  This contractual interface is used to provide a simple way to shuttle parcelable data and avoid app crashes
 *  from Transaction Too Large Exceptions. This interface is a factory that creates [ShuttleBundle] and
 *  [ShuttleIntent] cargo objects.  It is these objects which are created and access the repository to persist
 *  the data and return a small object to use in Android parcel transactions.  Additionally, this interface is
 *  a producer used to produce persisted [Bundle]s.  The intent of this interface is to enforce implementations
 *  to provide the consumer portion of the Producer - Consumer Design Pattern.
 *
 *  For more information on the factory design pattern, refer to:
 *  <a href="https://www.tutorialspoint.com/design_pattern/factory_pattern.htm">Factory Design Pattern</a>
 */
interface Shuttle {
    /**
     *
     */
    val shuttleFacade: ShuttleFacade

    /**
     *
     */
    val shuttleWarehouse: ShuttleWarehouse

    /**
     * This function creates a [ShuttleBundle].
     * @param bundle Used to create the [ShuttleBundle] object from.  For the other [bundleCargoWith] function,
     *               the bundle will be set to an empty bundle.
     * @param bundleFactory Used to create the bundle.  If the reference is null, it will be set to the
     *                      [DefaultBundleFactory] by default.
     * @return The newly created [ShuttleBundle]
     */
    fun bundleCargoWith(
        bundle: Bundle? = null,
        bundleFactory: BundleFactory? = DefaultBundleFactory()
    ): ShuttleBundle

    /**
     * This function creates a [ShuttleIntent].
     * @param intent Used by the [Intent]'s copy constructor to create a new intent
     * @return The newly created [ShuttleIntent]
     */
    fun intentCargoWith(intent: Intent): ShuttleIntent

    /**
     * This function creates a [ShuttleIntent].
     * @param action The Intent action, such as ACTION_VIEW.
     * @return The newly created [ShuttleIntent]
     */
    fun intentCargoWith(action: String): ShuttleIntent

    /**
     * This function creates a [ShuttleIntent].
     * @param action The Intent action, such as ACTION_VIEW.
     * @param uri The Intent data URI.
     * @return The newly created [ShuttleIntent]
     */
    fun intentCargoWith(action: String, uri: Uri): ShuttleIntent

    /**
     * This function creates a [ShuttleIntent].
     * @param packageContext A Context of the application package implementing this class.
     * @param cls The component class that is to be used for the intent.
     * @return The newly created [ShuttleIntent]
     */
    fun intentCargoWith(packageContext: Context, cls: Class<*>): ShuttleIntent

    /**
     * This function creates a [ShuttleIntent].
     * @param action Used to create a type of intent
     * @param uri The Intent data URI.
     * @param packageContext A Context of the application package implementing this class.
     * @param cls The component class that is to be used for the intent.
     * @return The newly created [ShuttleIntent]
     */
    fun intentCargoWith(
        action: String,
        uri: Uri,
        packageContext: Context,
        cls: Class<*>
    ): ShuttleIntent

    /**
     * This function creates a [ShuttleIntent].
     * @param target The Intent that the user will be selecting an activity to perform.
     * @param title Optional title that will be displayed in the chooser, only when the target
     *              action is not ACTION_SEND or ACTION_SEND_MULTIPLE.
     * @return The Intent object that you can hand to  [Context.startActivity] and related methods.
     * @see [Intent.createChooser]
     */
    fun intentChooserCargoWith(
        target: Intent,
        title: CharSequence?
    ): ShuttleIntent

    /**
     * This function creates a [ShuttleIntent].
     * @param target The Intent that the user will be selecting an activity to perform.
     * @param title Optional title that will be displayed in the chooser, only when the target action
     *              is not ACTION_SEND or ACTION_SEND_MULTIPLE.
     * @param sender Optional IntentSender to be called when a choice is made.
     * @return The Intent object that you can hand to  [Context.startActivity] and related methods.
     * @see [Intent.createChooser]
     */
    fun intentChooserCargoWith(
        target: Intent?,
        title: CharSequence?,
        sender: IntentSender?
    ): ShuttleIntent

    /**
     * Obtains the [Parcelable] cargo from the Warehouse.  When it is picked up, the cargo is completely
     * removed from the Warehouse.  For databases, this means deleting it from the db.
     * @param cargoId used to look up the cargo in the repository
     * @return the channel with a reference to the result, a [ShuttlePickupCargoResult]
     */
    suspend fun <D : Serializable> pickupCargo(cargoId: String): Channel<ShuttlePickupCargoResult>

    /**
     * Cleans the Shuttle on returning to the [currentScreen] from the [nextScreenClass] via the [cargoId].
     * @param currentScreen the current activity class reference
     * @param nextScreenClass the string for the name of the next screen's class
     * @param cargoId the id for the cargo shipped with Shuttle
     */
    fun cleanShuttleOnReturnTo(
        currentScreen: Class<*>,
        nextScreenClass: Class<*>,
        cargoId: String
    ): Shuttle

    /**
     * Removes cargo from the warehouse where the cargo matches the id.
     * @param cargoId the id for the cargo shipped with Shuttle
     */
    fun cleanShuttleFromDeliveryFor(cargoId: String, receiver: Channel<ShuttleRemoveCargoResult>? = null): Shuttle

    /**
     * Performs completion events for shuttle, such as clearing the database.
     */
    fun cleanShuttleFromAllDeliveries(receiver: Channel<ShuttleRemoveCargoResult>? = null): Shuttle
}
