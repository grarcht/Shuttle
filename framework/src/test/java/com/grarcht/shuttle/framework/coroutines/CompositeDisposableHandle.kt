package com.grarcht.shuttle.framework.coroutines

import kotlinx.coroutines.DisposableHandle

/**
 * A test utility that collects multiple [DisposableHandle] instances and disposes them all in a
 * single call, simplifying coroutine cleanup in test teardown.
 */
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
