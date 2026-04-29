package com.bong.reminder.list.adapter.`in`.web.dto

import java.time.Instant

data class ReminderListResponse(
    val id: Long,
    val name: String,
    val color: String,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
