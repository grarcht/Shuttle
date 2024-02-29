package com.grarcht.shuttle.demo.mvvm.error

import com.grarcht.shuttle.framework.error.ShuttleServiceError

data class DemoError(
    val serviceName: String,
    val cargoId: String,
    val errorMessage: String,
    val error: Throwable
) : ShuttleServiceError