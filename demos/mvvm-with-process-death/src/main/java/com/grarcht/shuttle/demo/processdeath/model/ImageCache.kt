package com.grarcht.shuttle.demo.processdeath.model

import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.processdeath.model.ImageCache.imageModel

/**
 * Simulates storing cargo in memory — a common pattern that does NOT survive process death.
 * When Android kills the process, this object is cleared. On restoration, [imageModel] is null,
 * demonstrating data loss without Shuttle.
 */
object ImageCache {
    var imageModel: ImageModel? = null
}
