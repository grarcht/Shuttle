package com.grarcht.shuttle.framework.validator

/**
 *
 */
interface ShuttleValidator<D> {

    /**
     * @param data
     */
    fun validate(data: D) : Boolean
}