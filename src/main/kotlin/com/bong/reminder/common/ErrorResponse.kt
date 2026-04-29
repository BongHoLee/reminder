package com.bong.reminder.common

data class ErrorResponse(
    val code: String,
    val message: String,
    val fieldErrors: List<FieldError> = emptyList(),
) {
    data class FieldError(
        val field: String,
        val message: String,
    )
}
