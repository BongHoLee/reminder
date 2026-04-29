package com.bong.reminder.list.adapter.`in`.web

import com.bong.reminder.list.adapter.`in`.web.dto.ReminderListCreateRequest
import com.bong.reminder.list.adapter.`in`.web.dto.ReminderListResponse
import com.bong.reminder.list.adapter.`in`.web.dto.ReminderListUpdateRequest
import com.bong.reminder.list.application.command.CreateReminderListCommand
import com.bong.reminder.list.application.command.UpdateReminderListCommand
import com.bong.reminder.list.application.query.ReminderListView

fun ReminderListCreateRequest.toCommand(): CreateReminderListCommand =
    CreateReminderListCommand(
        name = name,
        color = color,
        sortOrder = sortOrder,
    )

fun ReminderListUpdateRequest.toCommand(id: Long): UpdateReminderListCommand =
    UpdateReminderListCommand(
        id = id,
        name = name,
        color = color,
        sortOrder = sortOrder,
    )

fun ReminderListView.toResponse(): ReminderListResponse =
    ReminderListResponse(
        id = id,
        name = name,
        color = color,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
