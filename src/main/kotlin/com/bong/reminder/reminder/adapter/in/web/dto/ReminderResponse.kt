package com.bong.reminder.reminder.adapter.`in`.web.dto

import com.bong.reminder.reminder.domain.Priority
import java.time.Instant

data class ReminderResponse(
    val id: Long,
    val listId: Long,
    val parentId: Long?,
    val title: String,
    val notes: String?,
    val dueAt: Instant?,
    val priority: Priority,
    val completed: Boolean,
    val completedAt: Instant?,
    val flagged: Boolean,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
