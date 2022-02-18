package com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel

import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.io.RawResourceGateway
import kotlinx.coroutines.flow.Flow

/**
 * The MVVM ViewModel used with the First View and corresponding model.
 */
class FirstViewModel : ViewModel() {
    fun getImage(resources: Resources, @RawRes imageId: Int): Flow<IOResult> {
        return RawResourceGateway.with(resources).bytesFromRawResource(imageId).create()
    }
}
