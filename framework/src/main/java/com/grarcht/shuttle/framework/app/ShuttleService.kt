package com.grarcht.shuttle.framework.app

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Message
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.os.ShuttleBinder
import com.grarcht.shuttle.framework.os.messenger.ShuttleMessageReceiver
import com.grarcht.shuttle.framework.os.messenger.ShuttleMessengerDecorator
import java.io.Serializable

/**
 * The base service class for services to leverage Shuttle to transport cargo data.
 */
open class ShuttleService : Service(), ShuttleMessageReceiver {
    /**
     * The configuration for the service.
     */
    open lateinit var config: ShuttleServiceConfig

    /**
     * Clients use this binder for bound (local & IPC) services.
     */
    open var binder: ShuttleBinder<ShuttleService>? = null

    /**
     * Clients use this messenger to send messages to this service.
     */
    open var ipcServiceMessengerDecorator: ShuttleMessengerDecorator? = null

    /**
     * @see [Service.onStartCommand]
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        createBinder()
        return START_REDELIVER_INTENT
    }

    /**
     * @see [Service.onBind]
     */
    override fun onBind(intent: Intent?): IBinder? {
        return createBinder()
    }

    /**
     * @see [Service.onRebind]
     */
    override fun onRebind(intent: Intent?) {
        createBinder()
    }

    /**
     * @see [Service.onUnbind]
     */
    override fun onUnbind(intent: Intent?): Boolean {
        return config.rebindOnUnbind
    }

    /**
     * Release resources.
     */
    override fun onDestroy() {
        releaseResourcesForLocalServices()
        releaseResourcesForIPCServices()
        super.onDestroy()
    }

    private fun createBinder(): IBinder? {
        return if (config.bindingType.isMessengerBoundService()) {
            createMessengerDecoratorForIPC()
        } else {
            createBinderForLocalBoundService()
        }
    }

    /**
     * Creates the binder object for binding with local, non-IPC services.
     */
    open fun createBinderForLocalBoundService(): ShuttleBinder<ShuttleService>? {
        if (config.bindingType.isLocalBoundService()) {
            binder = ShuttleBinder(this)
        }
        return binder
    }

    /**
     * Initialize the messenger decorator for Inter-Process Communication, meaning the service and app are in different
     * processes and a messenger will need to be used.
     */
    open fun createMessengerDecoratorForIPC(): IBinder? {
        return if (config.bindingType == ShuttleServiceType.BOUND_MESSENGER) {
            if (ipcServiceMessengerDecorator == null)
                ipcServiceMessengerDecorator = config.messengerFactory.createMessenger(
                    mainLooper,
                    config.serviceName,
                    messageReceiver = this,
                    config.errorObservable,
                    config.messageValidator
                )
            ipcServiceMessengerDecorator?.getBinder()
        } else {
            null
        }
    }

    /**
     * Releases resources, including cleaning shuttle from all deliveries used by the IPC service.
     */
    open fun releaseResourcesForIPCServices() {
        if (config.bindingType == ShuttleServiceType.BOUND_MESSENGER) {
            ipcServiceMessengerDecorator?.let {
                val cargoIds = it.cargoIds
                for (cargoId in cargoIds) {
                    config.shuttle.cleanShuttleFromDeliveryFor(cargoId)
                }
                it.clearCargoIds()
                ipcServiceMessengerDecorator = null
            }
            binder = null
        }
    }

    /**
     * Releases resources for local, non-iPC services.
     */
    open fun releaseResourcesForLocalServices() {
        if (config.bindingType == ShuttleServiceType.BOUND_LOCAL) {
            binder = null
        }
    }

    /**
     * Transports the cargo using Shuttle and broadcasts.
     * @param cargoId of the cargo to transport
     * @param cargo to transport
     */
    open fun <D : Serializable> transportCargoWithShuttle(
        cargoId: String,
        cargo: D?
    ) {
        val cargoIntent = getCargoIntentForTransport(cargoId, cargo)
        sendBroadcast(cargoIntent)
    }

    /**
     * Override this function to provide the cargo intent, used for transporting cargo with [Shuttle]. What is
     * provided below is the default implementation.
     */
    open fun <D : Serializable> getCargoIntentForTransport(
        cargoId: String,
        cargo: D?
    ): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        return config.shuttle.intentCargoWith(intent)
            .logTag(getServiceName())
            .transport(cargoId, cargo) // to the warehouse
            .create()
    }

    /**
     * Override this function to provide a human-readable string. For obfuscated apps, this string will become
     * garbled since reflection is used to get the name of the class. When looking at log tags in log management
     * systems, the tag will not be readable. The recommended approach is to use the name of your service in a
     * string.  For instance, if your service is named MyService, then return "MyService".
     */
    open fun getServiceName(): String = this::class.java.simpleName

    /**
     * Override this function to handle IPC messaging.
     *
     * Since this service supports local binding and messenger binding for IPC, and this function is for the
     * latter, then this function is open and not abstract.
     *
     *  @param messageWhat see [Message.what]
     *  @param msg see [Message]
     */
    @Suppress("unused", "EmptyMethod", "RedundantSuppression")
    override fun onReceiveMessage(messageWhat: Int, msg: Message) {
        // ignore
    }
}
