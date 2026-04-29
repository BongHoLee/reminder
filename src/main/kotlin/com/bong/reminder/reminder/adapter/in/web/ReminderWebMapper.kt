package com.bong.reminder.reminder.adapter.`in`.web

import com.bong.reminder.reminder.adapter.`in`.web.dto.ReminderCreateRequest
import com.bong.reminder.reminder.adapter.`in`.web.dto.ReminderResponse
import com.bong.reminder.reminder.adapter.`in`.web.dto.ReminderUpdateRequest
import com.bong.reminder.reminder.application.command.CreateReminderCommand
import com.bong.reminder.reminder.application.command.UpdateReminderCommand
import com.bong.reminder.reminder.application.query.ReminderView

fun ReminderCreateRequest.toCommand(listId: Long): CreateReminderCommand =
    CreateReminderCommand(
        listId = listId,
        title = title,
        notes = notes,
        dueAt = dueAt,
        priority = priority,
        flagged = flagged,
        sortOrder = sortOrder,
        parentId = parentId,
    )

fun ReminderUpdateRequest.toCommand(id: Long): UpdateReminderCommand =
    UpdateReminderCommand(
        id = id,
        title = title,
        notes = notes,
        dueAt = dueAt,
        priority = priority,
        flagged = flagged,
        sortOrder = sortOrder,
        parentId = parentId,
    )

fun ReminderView.toResponse(): ReminderResponse =
    ReminderResponse(
        id = id,
        listId = listId,
        parentId = parentId,
        title = title,
        notes = notes,
        dueAt = dueAt,
        priority = priority,
        completed = completed,
        completedAt = completedAt,
        flagged = flagged,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
