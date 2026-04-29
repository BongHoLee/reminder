package com.bong.reminder.list.application.port.`in`

import com.bong.reminder.list.application.query.ReminderListView

interface ReminderListQueryService {
    fun findAll(): List<ReminderListView>
}
