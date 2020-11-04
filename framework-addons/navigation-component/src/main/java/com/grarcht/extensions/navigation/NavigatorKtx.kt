package com.grarcht.extensions.navigation

import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.grarcht.shuttle.framework.model.ShuttleParcelPackage
import com.grarcht.shuttle.framework.respository.ShuttleWarehouse

fun <D : NavDestination, N : Navigator<D>> N.navigateSafely(
    repository: ShuttleWarehouse,
    destination: D,
    args: Bundle?,
    navOptions: NavOptions?,
    navigatorExtras: Navigator.Extras?
) {
    shuttleParcels(repository, args)
    navigate(destination, args, navOptions, navigatorExtras)
}

private fun shuttleParcels(repository: ShuttleWarehouse, args: Bundle?) {
    if (args != null) {
        val keys = args.keySet()
        val iterator = keys.iterator()
        iterator.forEach { key ->
            val arg: Any? = args.get(key)
            if (arg is Parcelable) {
                val parcelPackage = ShuttleParcelPackage(repository.id, key)
                args.putParcelable(key, parcelPackage)
            }
        }
    }
}