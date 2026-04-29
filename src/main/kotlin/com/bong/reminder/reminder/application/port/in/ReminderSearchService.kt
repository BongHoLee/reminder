package com.bong.reminder.reminder.application.port.`in`

import com.bong.reminder.reminder.application.query.ReminderView

interface ReminderSearchService {
    fun search(query: String): List<ReminderView>
}
