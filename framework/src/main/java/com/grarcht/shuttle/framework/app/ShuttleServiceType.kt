package com.grarcht.shuttle.framework.app

/**
 * Used to determine the binder to create and which type of service the app is bound to.
 */
enum class ShuttleServiceType(private val typeName: String) {
    BOUND_AIDL("aidl"),
    BOUND_LOCAL("local"),
    BOUND_MESSENGER("messenger"),
    NON_BOUND_STARTED("non_bound_started");

    /**
     * @return true of the service is of type [BOUND_AIDL], [BOUND_LOCAL], or [BOUND_MESSENGER].
     */
    @Suppress("unused")
    fun isBoundService() = this.typeName == BOUND_AIDL.typeName ||
        this.typeName == BOUND_LOCAL.typeName ||
        this.typeName == BOUND_MESSENGER.typeName

    /**
     * @return true of the service is of type [BOUND_AIDL].
     */
    @Suppress("unused")
    fun isAIDLBoundService() = this.typeName == BOUND_AIDL.typeName

    /**
     * @return true of the service is of type [BOUND_LOCAL].
     */
    @Suppress("unused")
    fun isLocalBoundService() = this.typeName == BOUND_LOCAL.typeName

    /**
     * @return true of the service is of type [BOUND_MESSENGER].
     */
    @Suppress("unused")
    fun isMessengerBoundService() = this.typeName == BOUND_MESSENGER.typeName

    /**
     * @return true of the service is of type [NON_BOUND_STARTED].
     */
    @Suppress("unused")
    fun isNonBoundService() = this.typeName == NON_BOUND_STARTED.typeName
}
