package com.grarcht.shuttle.framework.app

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Message
import com.grarcht.shuttle.framework.os.ShuttleBinder
import com.grarcht.shuttle.framework.os.messenger.ShuttleMessengerDecorator
import java.io.Serializable

/**
 * The base service class for services to leverage Shuttle to transport cargo data.
 * @param config the configuration for the service
 */
open class ShuttleService(
    open val config: ShuttleServiceConfig
) : Service() {
    /**
     * Clients use this binder for local, non-IPC services.
     */
    open var localServiceBinder: ShuttleBinder<ShuttleService>? = null

    /**
     * Clients use this messenger to send messages to this service.
     */
    open var ipcServiceMessengerDecorator: ShuttleMessengerDecorator? = null

    /**
     * Create the local service binder if this service is used as a local service.
     */
    override fun onCreate() {
        super.onCreate()
        createBinderForALocalService()
    }

    /**
     * @see [Service.onStartCommand]
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT
    }

    /**
     * @see [Service.onBind]
     */
    override fun onBind(intent: Intent?): IBinder? {
        return if (localServiceBinder == null) {
            localServiceBinder
        } else {
            initMessengerDecoratorForIPC()
        }
    }

    /**
     * @see [Service.onRebind]
     */
    override fun onRebind(intent: Intent?) {
        initMessengerDecoratorForIPC()
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

    /**
     * Creates the binder object for binding with local, non-IPC services.
     */
    open fun createBinderForALocalService() {
        if (config.bindingType == ShuttleServiceBindingType.LOCAL) {
            localServiceBinder = ShuttleBinder(this)
        }
    }

    /**
     * Initialize the messenger decorator for Inter-Process Communication, meaning the service and app are in different
     * processes and a messenger will need to be used.
     */
    open fun initMessengerDecoratorForIPC(): IBinder? {
        return if (config.bindingType == ShuttleServiceBindingType.MESSENGER) {
            ipcServiceMessengerDecorator = if (ipcServiceMessengerDecorator != null)
                ipcServiceMessengerDecorator
            else
                config.messengerFactory.createMessenger(
                    mainLooper,
                    config.serviceName,
                    context = this,
                    shuttleService = this,
                    config.errorObservable
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
        if (config.bindingType == ShuttleServiceBindingType.MESSENGER) {
            ipcServiceMessengerDecorator?.let {
                val cargoIds = it.cargoIds
                for (cargoId in cargoIds) {
                    config.shuttle.cleanShuttleFromDeliveryFor(cargoId)
                }
                it.clearCargoIds()
                ipcServiceMessengerDecorator = null
            }
        }
    }

    /**
     * Releases resources for local, non-iPC services.
     */
    open fun releaseResourcesForLocalServices() {
        if (config.bindingType == ShuttleServiceBindingType.LOCAL) {
            localServiceBinder = null
        }
    }

    /**
     * Transports the cargo using Shuttle and broadcasts.
     * @param cargoId of the cargo to transport
     * @param cargo to transport
     */
    open fun <D : Serializable> transportCargoWithShuttle(cargoId: String, cargo: D?) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val cargoIntent = config.shuttle.intentCargoWith(intent)
            .logTag(getServiceName())
            .transport(cargoId, cargo) // to the warehouse
            .create()
        super.sendBroadcast(cargoIntent)
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
     */
    @Suppress("unused", "EmptyMethod", "RedundantSuppression")
    open fun onReceiveMessage(context: Context, messageWhat: Int, msg: Message) {
        // ignore
    }
}
