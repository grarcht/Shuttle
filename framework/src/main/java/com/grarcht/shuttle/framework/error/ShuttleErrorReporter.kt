package com.grarcht.shuttle.framework.error

interface ShuttleErrorReporter {
    fun report(error: ShuttleError)
}