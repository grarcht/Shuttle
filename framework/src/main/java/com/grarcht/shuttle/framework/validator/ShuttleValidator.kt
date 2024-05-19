package com.grarcht.shuttle.framework.validator

/**
 * Validates data.
 */
interface ShuttleValidator<D> {

    /**
     * Validates the [data].
     * @param data to validate
     *
     * @return true if the data is valid.
     */
    fun validate(data: D): Boolean
}