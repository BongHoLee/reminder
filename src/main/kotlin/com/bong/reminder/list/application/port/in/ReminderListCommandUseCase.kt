package com.bong.reminder.list.application.port.`in`

import com.bong.reminder.list.dto.ReminderListCreateRequest
import com.bong.reminder.list.dto.ReminderListResponse
import com.bong.reminder.list.dto.ReminderListUpdateRequest

interface ReminderListCommandUseCase {
    fun create(request: ReminderListCreateRequest): ReminderListResponse
    fun update(id: Long, request: ReminderListUpdateRequest): ReminderListResponse
    fun delete(id: Long)
}
