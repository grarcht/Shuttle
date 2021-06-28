package com.grarcht.shuttle.framework.coroutines

import kotlinx.coroutines.DisposableHandle

class CompositeDisposableHandle {
    private val disposables = mutableListOf<DisposableHandle>()

    fun add(disposable: DisposableHandle) {
        if (disposables.contains(disposable).not()) {
            disposables.add(disposable)
        }
    }

    fun dispose() {
        disposables.forEach { disposable ->
            disposable.dispose()
        }
    }
}
