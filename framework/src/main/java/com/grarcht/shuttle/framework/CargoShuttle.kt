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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val CLEAN_CARGO_CHANNEL_CAPACITY = 2
private const val NO_PICKUP_TIMEOUT = 0L
private const val PICKUP_WITH_TIMEOUT_CHANNEL_CAPACITY = 2

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
    override val shuttleWarehouse: ShuttleWarehouse,
    backgroundThreadDispatcher: CoroutineDispatcher = Dispatchers.IO
) : Shuttle {
    private val backgroundThreadScope = CoroutineScope(backgroundThreadDispatcher)

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
        return ShuttleIntent.with(shuttleWarehouse, shuttleFacade).intentChooser(target, title, sender)
    }

    /**
     * Obtains the [Parcelable] cargo from the database. When [timeoutMs] is greater than zero,
     * the pickup operation is bounded: if no terminal result is received within [timeoutMs]
     * milliseconds, a [ShuttlePickupCargoResult.Error] is emitted and the returned [Channel] is
     * closed automatically.
     * @param cargoId used to look up the cargo in the repository
     * @param timeoutMs maximum milliseconds to wait, or zero for no limit
     * @return the channel with a reference to the result, a [ShuttlePickupCargoResult]
     */
    override suspend fun <D : ShuttleCargoData> pickupCargo(
        cargoId: String,
        timeoutMs: Long
    ): Channel<ShuttlePickupCargoResult> {
        if (timeoutMs <= NO_PICKUP_TIMEOUT) {
            return shuttleWarehouse.pickup<D>(cargoId)
        }
        val channel = Channel<ShuttlePickupCargoResult>(PICKUP_WITH_TIMEOUT_CHANNEL_CAPACITY)
        try {
            withTimeout(timeoutMs) {
                shuttleWarehouse.pickup<D>(cargoId).consumeAsFlow().takeWhile { result ->
                    channel.send(result)
                    result !is ShuttlePickupCargoResult.Success<*> && result !is ShuttlePickupCargoResult.Error<*>
                }.collect {}
            }
        } catch (e: TimeoutCancellationException) {
            val errorMessage = "Pickup of cargo for cargoId: $cargoId timed out after ${timeoutMs}ms."
            channel.send(ShuttlePickupCargoResult.Error(cargoId, errorMessage, throwable = e))
        } finally {
            channel.close()
        }
        return channel
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
        backgroundThreadScope.launch {
            shuttleFacade.removeCargoAfterDelivery(currentScreen, nextScreenClass, cargoId)
        }
        return this
    }

    /**
     * Removes cargo from the warehouse where the cargo matches the [cargoId] and returns a
     * [Channel] that emits the removal progress and terminal result. The channel is closed
     * automatically when a terminal result is emitted so consumers' flows terminate naturally.
     * @param cargoId the id for the cargo shipped with Shuttle
     * @return a [Channel] emitting [ShuttleRemoveCargoResult] states for this removal operation
     */
    override fun cleanShuttleFromDeliveryFor(cargoId: String): Channel<ShuttleRemoveCargoResult> {
        val channel = Channel<ShuttleRemoveCargoResult>(CLEAN_CARGO_CHANNEL_CAPACITY)
        backgroundThreadScope.launch {
            try {
                val warehouseChannel = shuttleWarehouse.removeCargoBy(cargoId)
                for (result in warehouseChannel) {
                    channel.send(result)
                    when (result) {
                        is ShuttleRemoveCargoResult.Removed,
                        is ShuttleRemoveCargoResult.UnableToRemove<*>,
                        is ShuttleRemoveCargoResult.DoesNotExist -> {
                            break
                        }

                        else -> {
                            /* await next result */
                        }
                    }
                }
            } finally {
                channel.close()
            }
        }
        return channel
    }

    /**
     * Removes all cargo from the warehouse and returns a [Channel] that emits the removal
     * progress and terminal result. The channel is closed automatically when a terminal result
     * is emitted so consumers' flows terminate naturally.
     * @return a [Channel] emitting [ShuttleRemoveCargoResult] states for this removal operation
     */
    override fun cleanShuttleFromAllDeliveries(): Channel<ShuttleRemoveCargoResult> {
        val channel = Channel<ShuttleRemoveCargoResult>(CLEAN_CARGO_CHANNEL_CAPACITY)
        backgroundThreadScope.launch {
            try {
                val warehouseChannel = shuttleWarehouse.removeAllCargo()
                for (result in warehouseChannel) {
                    channel.send(result)
                    when (result) {
                        is ShuttleRemoveCargoResult.Removed,
                        is ShuttleRemoveCargoResult.UnableToRemove<*>,
                        is ShuttleRemoveCargoResult.DoesNotExist -> {
                            break
                        }

                        else -> {
                            /* await next result */
                        }
                    }
                }
            } finally {
                channel.close()
            }
        }
        return channel
    }
}
