package com.bong.reminder.list.domain

class ReminderListNotFoundException(
    message: String = "리스트를 찾을 수 없습니다.",
) : RuntimeException(message)
