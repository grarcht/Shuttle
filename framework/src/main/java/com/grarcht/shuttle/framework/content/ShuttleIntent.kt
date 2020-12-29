@file:Suppress("unused")

package com.grarcht.shuttle.framework.content

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable

private const val DEFAULT_LOG_TAG = "ShuttleIntent"

/**
 *  This class creates stores the intent's data cargo in the [ShuttleWarehouse]
 *  and creates an [Intent] that can be transported with avoiding the
 *  Transaction Too Large Exception.  This class uses the Fluent Interface Design
 *  Pattern to achieve function chaining.  For more information on this design
 *  pattern, refer to:
 *  <a href="https://en.wikipedia.org/wiki/Fluent_interface">Fluent Interface</a>
 */
open class ShuttleIntent private constructor() {
    private var intent: Intent? = null
    private var logTag: String? = null
    private var shuttleScreenFacade: ShuttleFacade? = null
    private var warehouse: ShuttleWarehouse? = null


    /**
     * Creates a [ShuttleIntent] to use with function chaining in the fluent interface.
     * @param intent Contains the data to shuttle.
     * @return The newly created object reference
     */
    fun intent(intent: Intent): ShuttleIntent {
        this.intent = intent
        return this
    }

    /**
     * Creates a [ShuttleIntent] to use with function chaining in the fluent interface.
     * @param action To create an intent from.  It is the Intent action, such as ACTION_VIEW.
     * @return The newly created object reference
     */
    fun intent(action: String): ShuttleIntent {
        this.intent = Intent(action)
        return this
    }

    /**
     * Creates a [ShuttleIntent] to use with function chaining in the fluent interface.
     * @param action To create an intent from.  It is the Intent action, such as ACTION_VIEW.
     * @return The newly created object reference
     */
    fun intent(action: String, uri: Uri): ShuttleIntent {
        this.intent = Intent(action, uri)
        return this
    }

    /**
     * Creates a [ShuttleIntent] to use with function chaining in the fluent interface.
     * @param packageContext A Context of the application package implementing this class.
     * @param cls The component class that is to be used for the intent.
     * @return The newly created object reference
     */
    fun intent(packageContext: Context, cls: Class<*>): ShuttleIntent {
        this.intent = Intent(packageContext, cls)
        return this
    }

    /**
     * Creates a [ShuttleIntent] to use with function chaining in the fluent interface.
     * @param action To create an intent from.  It is the Intent action, such as ACTION_VIEW.
     * @param uri The Intent data URI.
     * @param packageContext A Context of the application package implementing this class.
     * @param cls The component class that is to be used for the intent.
     * @return The newly created object reference
     */
    fun intent(action: String, uri: Uri, packageContext: Context, cls: Class<*>): ShuttleIntent {
        this.intent = Intent(action, uri, packageContext, cls)
        return this
    }

    /**
     * Creates a [ShuttleIntent] to use with function chaining in the fluent interface.
     * @param target The Intent that the user will be selecting an activity to perform.
     * @param title Optional title that will be displayed in the chooser, only when the
     *              target action is not ACTION_SEND or ACTION_SEND_MULTIPLE.
     * @return The newly created object reference
     */
    fun intentChooser(target: Intent?, title: CharSequence?): ShuttleIntent {
        val shuttleIntent = ShuttleIntent()
        shuttleIntent.intent = Intent.createChooser(target, title)
        shuttleIntent.warehouse = warehouse
        shuttleIntent.shuttleScreenFacade = shuttleScreenFacade
        return shuttleIntent
    }

    /**
     * Creates a [ShuttleIntent] to use with function chaining in the fluent interface.
     * @param target The Intent that the user will be selecting an activity to perform.
     * @param title Optional title that will be displayed in the chooser, only when the
     *              target action is not ACTION_SEND or ACTION_SEND_MULTIPLE.
     * @return The newly created object reference
     */
    fun intentChooser(target: Intent?, title: CharSequence?, sender: IntentSender?): ShuttleIntent {
        val shuttleIntent = ShuttleIntent()
        shuttleIntent.intent = Intent.createChooser(target, title, sender)
        shuttleIntent.warehouse = warehouse
        shuttleIntent.shuttleScreenFacade = shuttleScreenFacade
        return shuttleIntent
    }

    /**
     * Sets the [data] for transport.
     * @param cargoId the key used for shuttle the cargo to and from the [ShuttleWarehouse]
     * @param data the cargo to shuttle
     * @return the [ShuttleIntent] reference use with function chaining
     */
    @Throws(IllegalStateException::class)
    fun <D : Serializable> transport(cargoId: String, data: D?): ShuttleIntent {
        verifyIntentFunctionWasCalled()
        storeParcelPackageAsIntentData(cargoId)
        GlobalScope.launch {
            warehouse?.store(cargoId, data)
        }.invokeOnCompletion {
            it?.let { throwable ->
                Log.e(
                    logTag, "There was an issues when transporting the data with the " +
                            "Shuttle Intent.", throwable
                )
            }
        }

        return this
    }

    /**
     * The specific tag to use if there is an issue with the [ShuttleIntent] functionality.
     */
    fun logTag(tag: String?): ShuttleIntent {
        logTag = tag ?: DEFAULT_LOG_TAG
        return this
    }

    /**
     * Cleans the Shuttle on returning to the [currentScreenClass] from the [nextScreenClass] via
     * the [cargoId].
     * @param currentScreenClass the current activity class reference
     * @param nextScreenClass the string for the name of the next screen's class
     * @param cargoId the id for the cargo shipped with Shuttle
     */
    fun cleanShuttleOnReturnTo(
        currentScreenClass: Class<*>,
        nextScreenClass: Class<*>,
        cargoId: String
    ): ShuttleIntent {
        shuttleScreenFacade?.apply {
            GlobalScope.launch {
                removeCargoAfterDelivery(currentScreenClass, nextScreenClass, cargoId)
            }
        }
        return this
    }

    /**
     * Navigates to the next screen via a call to [Context.startActivity].
     * This is a terminal function for the fluent interface.
     * @param context used to navigate to the next activity
     */
    @Throws(IllegalStateException::class)
    fun deliver(context: Context) {
        verifyIntentFunctionWasCalled()
        context.startActivity(intent)
    }

    /**
     * Completes the creation of the [Intent], to use with functions like [Context.startActivity].
     * This is a terminal function for the fluent interface.
     */
    @Throws(IllegalStateException::class)
    fun create(): Intent {
        verifyIntentFunctionWasCalled()
        return intent as Intent
    }

    private fun verifyIntentFunctionWasCalled() {
        if (intent == null) {
            throw IllegalStateException("$logTag.  Double check the usage of the fluid interface.  The intent is null.")
        }
    }

    private fun storeParcelPackageAsIntentData(cargoId: String) {
        val parcelPackage = ShuttleParcelCargo(cargoId)
        intent?.putExtra(cargoId, parcelPackage)
    }

    companion object {
        /**
         * Creates a [ShuttleIntent] to use with function chaining in the fluent interface.
         * @param warehouse used to store the shuttled cargo
         * @return The newly created object reference
         */
        fun with(
            warehouse: ShuttleWarehouse,
            shuttleScreenFacade: ShuttleFacade? = null
        ): ShuttleIntent {
            val shuttleIntent = ShuttleIntent()
            shuttleIntent.warehouse = warehouse
            shuttleIntent.shuttleScreenFacade = shuttleScreenFacade
            return shuttleIntent
        }
    }
}

