package com.bong.reminder.reminder.domain

class ReminderNotFoundException(
    message: String = "미리 알림을 찾을 수 없습니다.",
) : RuntimeException(message)
