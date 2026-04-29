package com.bong.reminder.reminder.application.command

import com.bong.reminder.reminder.domain.Priority
import java.time.Instant

data class CreateReminderCommand(
    val listId: Long,
    val title: String,
    val notes: String? = null,
    val dueAt: Instant? = null,
    val priority: Priority = Priority.NONE,
    val flagged: Boolean = false,
    val sortOrder: Int = 0,
    val parentId: Long? = null,
)
