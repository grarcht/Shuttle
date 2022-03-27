package com.grarcht.shuttle.framework.coroutines

import kotlinx.coroutines.DisposableHandle

fun DisposableHandle?.addForDisposal(compositeDisposable: CompositeDisposableHandle?) {
    this?.let {
        compositeDisposable?.add(it)
    }
}
