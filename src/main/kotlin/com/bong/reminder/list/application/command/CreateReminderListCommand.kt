package com.bong.reminder.list.application.command

data class CreateReminderListCommand(
    val name: String,
    val color: String,
    val sortOrder: Int,
)
