package com.bong.reminder.reminder.application.port.`in`

import com.bong.reminder.reminder.application.command.CreateReminderCommand
import com.bong.reminder.reminder.application.command.DeleteReminderCommand
import com.bong.reminder.reminder.application.command.ToggleReminderCompletedCommand
import com.bong.reminder.reminder.application.command.UpdateReminderCommand
import com.bong.reminder.reminder.application.query.ReminderView

interface ReminderCommandService {
    fun create(command: CreateReminderCommand): ReminderView
    fun update(command: UpdateReminderCommand): ReminderView
    fun toggleCompleted(command: ToggleReminderCompletedCommand): ReminderView
    fun delete(command: DeleteReminderCommand)
}
