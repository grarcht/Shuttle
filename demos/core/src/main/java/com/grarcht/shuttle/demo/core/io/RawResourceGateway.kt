package com.grarcht.shuttle.demo.core.io

import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.util.Log
import androidx.annotation.RawRes
import com.grarcht.shuttle.framework.integrations.persistence.io.closeQuietly
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream

private const val DEFAULT_LOG_TAG = "ImageGateway"

/**
 * This class is responsible for getting the bytes for a raw resources using the Gateway and
 * Fluent Interface Design Patterns.  For more information on these design patterns refer to:
 * <a href="https://microservices.io/patterns/apigateway.html">Gateway Design Pattern</a>
 * <a href="https://en.wikipedia.org/wiki/Fluent_interface">Fluent Interface Design Pattern</a>
 */
class RawResourceGateway(private var resources: Resources) {
    private var flow: Flow<IOResult>? = null
    private var inputStream: InputStream? = null
    private var bufferedInputStream: BufferedInputStream? = null
    private var logTag: String? = null

    /**
     * Adds a log tag to use for logging errors when getting the [ByteArray] for raw [Resources].
     * @return the gateway object reference to use with function chaining
     * @see [bytesFromRawResource]
     */
    fun logTag(tag: String): RawResourceGateway {
        logTag = tag
        return this
    }

    /**
     * Gets the bytes from the raw resource, defined by [resId].
     * @return the gateway object reference to use with function chaining
     */
    @Suppress("SpellCheckingInspection")
    fun bytesFromRawResource(@RawRes resId: Int): RawResourceGateway {
        this.flow = flow {
            emit(IOResult.Loading)
            logTag = logTag ?: DEFAULT_LOG_TAG

            try {
                inputStream = resources.openRawResource(resId)
                inputStream?.let {
                    bufferedInputStream = BufferedInputStream(inputStream)
                    val byteArray = bufferedInputStream?.readBytes()
                    emit(IOResult.Success(byteArray))
                }
            } catch (rnfe: NotFoundException) {
                Log.e(logTag, "Unable to load byte array", rnfe)
                emit(IOResult.Error(throwable = rnfe))
            } catch (ioe: IOException) {
                Log.e(logTag, "Unable to load byte array", ioe)
                emit(IOResult.Error(throwable = ioe))
            } finally {
                bufferedInputStream.closeQuietly()
                inputStream.closeQuietly()
            }
        }
        return this
    }

    /**
     * Creates the [Flow] object reference for emitting [IOResult].
     * @return the newly created reference
     */
    fun create(): Flow<IOResult> {
        return if (flow == null) {
            flow {
                emit(IOResult.Error(throwable = Exception("Function ")))
            }
        } else {
            flow as Flow<IOResult>
        }
    }

    companion object {
        /**
         * Starts the fluent interface chaining to create get the bytes for a raw resource.
         * @param resources used to access raw resources
         * @return the gateway object reference to use with function chaining
         */
        fun with(resources: Resources): RawResourceGateway {
            return RawResourceGateway((resources))
        }
    }
}
