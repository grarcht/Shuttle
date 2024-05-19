package com.grarcht.shuttle.demo.mvvmwithaservice.view

/**
 * Used to control the flow for the type of dialog to display.
 */
enum class DialogType(val typeValue: Int) {
    LOADING(0), CONTENT(1), ERROR(2);

    companion object {

        /**
         * Maps the type value to the [DIALOG_TYPE] and is used for UI flow.
         */
        fun toDialogType(value: Int): DialogType {
            return when (value) {
                CONTENT.typeValue -> CONTENT
                ERROR.typeValue -> ERROR
                else -> LOADING
            }
        }
    }
}