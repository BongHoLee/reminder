package com.bong.reminder.reminder.application.command

import com.bong.reminder.reminder.domain.Priority
import java.time.Instant

data class UpdateReminderCommand(
    val id: Long,
    val title: String? = null,
    val notes: String? = null,
    val dueAt: Instant? = null,
    val priority: Priority? = null,
    val flagged: Boolean? = null,
    val sortOrder: Int? = null,
    val parentId: Long? = null,
)
