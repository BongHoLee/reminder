package com.bong.reminder.list.application.command

data class UpdateReminderListCommand(
    val id: Long,
    val name: String? = null,
    val color: String? = null,
    val sortOrder: Int? = null,
)
