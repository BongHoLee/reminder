package com.bong.reminder.list.application.port.`in`

import com.bong.reminder.list.dto.ReminderListResponse

interface ReminderListQueryUseCase {
    fun findAll(): List<ReminderListResponse>
}
