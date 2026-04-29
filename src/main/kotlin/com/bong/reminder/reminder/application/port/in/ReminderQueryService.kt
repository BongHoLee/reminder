package com.bong.reminder.reminder.application.port.`in`

import com.bong.reminder.reminder.application.query.ReminderView

interface ReminderQueryService {
    fun findByList(listId: Long, completed: Boolean): List<ReminderView>
    fun findChildren(parentId: Long): List<ReminderView>
}
