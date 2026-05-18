package com.grarcht.shuttle.demo.mvvmwithaservice.view

/**
 * Used to control the flow for the type of dialog to display.
 */
enum class DialogType(val typeValue: Int) {
    LOADING(0), CONTENT(1), ERROR(2);

    companion object {

        /**
         * Maps [value] to the corresponding [DialogType], defaulting to [LOADING] for
         * unrecognized values.
         *
         * @param value the raw integer type value to map.
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
