package com.grarcht.shuttle.framework.content

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import com.grarcht.shuttle.framework.model.ShuttleParcelPackage
import com.grarcht.shuttle.framework.respository.ShuttleWarehouse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 *
 */
open class ShuttleIntent private constructor() {

    private var intent: Intent? = null
    private var repository: ShuttleWarehouse? = null
    private var logTag: String? = null

    /**
     *
     */
    @Throws(IllegalStateException::class)
    fun <D: Parcelable> safelyPutExtra(name: String, data: D?): ShuttleIntent {
        verifyWithFunctionWasCalled()
        storeParcelPackageAsIntentData(name)
        GlobalScope.launch {
            repository?.save(name, data)
        }.invokeOnCompletion {

        }

        return this
    }

    /**
     *
     */
    @Throws(IllegalStateException::class)
    fun <D: Parcelable> safelyPutArrayExtra(name: String, data: Array<D>?): ShuttleIntent {
        verifyWithFunctionWasCalled()
        storeParcelPackageAsIntentData(name)
        GlobalScope.launch {
            repository?.save(name, data)
        }.invokeOnCompletion {

        }
        return this
    }

    /**
     *
     */
    @Throws(IllegalStateException::class)
    fun <D: Parcelable> safelyPutParcelableArrayListExtra(
        name: String,
        data: ArrayList<D>?
    ): ShuttleIntent {
        verifyWithFunctionWasCalled()
        storeParcelPackageAsIntentData(name)
        GlobalScope.launch {
            repository?.save(name, data)
        }.invokeOnCompletion {

        }
        return this
    }

    fun logTag(tag: String?) {
        logTag = tag
    }

    /**
     *
     */
    @Throws(IllegalStateException::class)
    fun complete(): Intent {
        verifyWithFunctionWasCalled()
        return intent as Intent
    }

    private fun verifyWithFunctionWasCalled() {
        if (intent == null) {
            throw IllegalStateException("The function \"with(Intent, ParcelDepotRepository)\" must be called first")
        }
    }

    private fun storeParcelPackageAsIntentData(name: String) {
        repository?.let { repo ->
            val parcelPackage = ShuttleParcelPackage(repo.id, name)
            intent?.putExtra(name, parcelPackage)
        }
    }

    companion object {
        /**
         *
         */
        fun with(
            intent: Intent,
            repository: ShuttleWarehouse
        ): ShuttleIntent {
            val parcelIntent = ShuttleIntent()
            parcelIntent.intent = intent
            parcelIntent.repository = repository
            return parcelIntent
        }

        /**
         *
         */
        fun with(
            action: String?,
            repository: ShuttleWarehouse
        ): ShuttleIntent {
            val parcelIntent = ShuttleIntent()
            parcelIntent.intent = Intent(action)
            parcelIntent.repository = repository
            return parcelIntent
        }

        /**
         *
         */
        fun with(
            action: String?,
            uri: Uri?,
            repository: ShuttleWarehouse
        ): ShuttleIntent {
            val parcelIntent = ShuttleIntent()
            parcelIntent.intent = Intent(action, uri)
            parcelIntent.repository = repository
            return parcelIntent
        }

        /**
         *
         */
        fun with(
            packageContext: Context?,
            cls: Class<*>,
            repository: ShuttleWarehouse
        ): ShuttleIntent? {
            return if (validateContextIsNotNoll(packageContext)) {
                val parcelIntent = ShuttleIntent()
                parcelIntent.intent = Intent(packageContext, cls)
                parcelIntent.repository = repository
                return parcelIntent
            } else {
                null
            }
        }

        /**
         *
         */
        fun with(
            action: String?,
            uri: Uri?,
            packageContext: Context?,
            cls: Class<*>?,
            repository: ShuttleWarehouse
        ): ShuttleIntent {
            val parcelIntent = ShuttleIntent()
            parcelIntent.intent = Intent(action, uri, packageContext, cls)
            parcelIntent.repository = repository
            return parcelIntent
        }

        /**
         *
         */
        fun <D> createChooser(
            target: Intent,
            title: CharSequence?,
            repository: ShuttleWarehouse
        ): ShuttleIntent {
            val parcelIntent = ShuttleIntent()
            parcelIntent.intent = Intent.createChooser(target, title)
            parcelIntent.repository = repository
            return parcelIntent
        }

        /**
         *
         */
        fun <D> createChooser(
            target: Intent?,
            title: CharSequence?,
            sender: IntentSender?,
            repository: ShuttleWarehouse
        ): ShuttleIntent {
            val parcelIntent = ShuttleIntent()
            parcelIntent.intent = Intent.createChooser(target, title, sender)
            parcelIntent.repository = repository
            return parcelIntent
        }

        private fun validateContextIsNotNoll(context: Context?, logTag: String? = null): Boolean {
            val tag = logTag ?: ShuttleIntent::class.java.simpleName
            val errorMessage = "Could not create parcel intent."

            return if (null == context) {
                Log.w(tag, errorMessage)
                false
            } else {
                true
            }
        }
    }
}