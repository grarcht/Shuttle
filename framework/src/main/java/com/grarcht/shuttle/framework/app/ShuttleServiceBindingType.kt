package com.grarcht.shuttle.framework.app

enum class ShuttleServiceBindingType(private val typeName: String) {
    LOCAL("local"),
    MESSENGER("messenger");

    fun isLocalService() = this.name == LOCAL.typeName
    fun isMessengerService() = this.name == MESSENGER.typeName
}