package com.grarcht.shuttle.demo.shuttle

import android.content.Intent
import com.grarcht.shuttle.framework.content.ShuttleDataExtractor
import com.grarcht.shuttle.framework.respository.ShuttleWarehouse

class ShuttleDataExtractorFactory {
    fun create(
        intent: Intent,
        shuttleWarehouse: ShuttleWarehouse
    ): ShuttleDataExtractor {
        return ShuttleDataExtractor(intent, shuttleWarehouse)
    }
}