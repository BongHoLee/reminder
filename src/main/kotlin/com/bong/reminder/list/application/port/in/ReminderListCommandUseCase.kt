package com.bong.reminder.list.application.port.`in`

import com.bong.reminder.list.application.command.CreateReminderListCommand
import com.bong.reminder.list.application.command.DeleteReminderListCommand
import com.bong.reminder.list.application.command.UpdateReminderListCommand
import com.bong.reminder.list.application.query.ReminderListView

interface ReminderListCommandUseCase {
    fun create(command: CreateReminderListCommand): ReminderListView
    fun update(command: UpdateReminderListCommand): ReminderListView
    fun delete(command: DeleteReminderListCommand)
}
