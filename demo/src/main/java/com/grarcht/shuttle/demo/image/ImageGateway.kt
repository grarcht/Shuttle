package com.grarcht.shuttle.demo.image

import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.util.Log
import androidx.annotation.RawRes
import com.grarcht.shuttle.demo.io.IOResult
import com.grarcht.shuttle.demo.io.closeQuietly
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception

class ImageGateway(private var resources: Resources?) {
    private var flow: Flow<IOResult>? = null
    private var inputStream: InputStream? = null
    private var bufferedInputStream: BufferedInputStream? = null
    private var logTag: String? = null

    fun logTag(tag: String): ImageGateway {
        logTag = tag
        return this
    }

    fun bytesFromRawResource(
        @RawRes resId: Int
    ): ImageGateway {
        this.flow = flow {
            if (null == resources) {
                emit(IOResult.Error(Exception("The resources object reference is null.")))
            } else {
                emit(IOResult.Loading)

                logTag = logTag ?: ImageGateway::class.java.simpleName

                try {
                    inputStream = resources?.openRawResource(resId)
                    inputStream?.let {
                        bufferedInputStream = BufferedInputStream(inputStream)
                        val byteArray = bufferedInputStream?.readBytes()
                        emit(IOResult.Success(byteArray))
                    }
                } catch (rnfe: NotFoundException) {
                    Log.e(logTag, "Unable to load byte array", rnfe)
                    emit(IOResult.Error(rnfe))
                } catch (ioe: IOException) {
                    Log.e(logTag, "Unable to load byte array", ioe)
                    emit(IOResult.Error(ioe))
                } finally {
                    bufferedInputStream.closeQuietly()
                    inputStream.closeQuietly()
                }
            }
        }
        return this
    }

    fun create(): Flow<IOResult> {
        return if (flow == null) {
            flow {
                emit(IOResult.Error(Exception("Function ")))
            }
        } else {
            flow as Flow<IOResult>
        }
    }

    companion object {
        fun with(resources: Resources): ImageGateway {
            return ImageGateway((resources))
        }
    }
}