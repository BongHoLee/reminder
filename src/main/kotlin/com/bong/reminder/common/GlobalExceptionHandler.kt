package com.bong.reminder.common

import com.bong.reminder.list.domain.ReminderListNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.map {
            ErrorResponse.FieldError(
                field = it.field,
                message = it.defaultMessage ?: "유효하지 않은 값입니다.",
            )
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = "VALIDATION_ERROR",
                    message = "요청 값이 유효하지 않습니다.",
                    fieldErrors = fieldErrors,
                ),
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = "INVALID_ARGUMENT",
                    message = ex.message ?: "잘못된 요청입니다.",
                ),
            )

    @ExceptionHandler(ReminderListNotFoundException::class)
    fun handleNotFound(ex: ReminderListNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    code = "REMINDER_LIST_NOT_FOUND",
                    message = ex.message ?: "리스트를 찾을 수 없습니다.",
                ),
            )
}
