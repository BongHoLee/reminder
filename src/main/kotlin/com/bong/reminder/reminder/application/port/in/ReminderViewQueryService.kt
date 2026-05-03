package com.bong.reminder.reminder.application.port.`in`

import com.bong.reminder.reminder.application.query.ReminderView
import com.bong.reminder.reminder.application.query.SmartViewCounts
import java.time.ZoneId

interface ReminderViewQueryService {
    fun today(zone: ZoneId, limit: Int = 1000): List<ReminderView>
    fun scheduled(zone: ZoneId): List<ReminderView>
    fun all(): List<ReminderView>
    fun flagged(): List<ReminderView>
    fun completed(): List<ReminderView>
    fun counts(zone: ZoneId): SmartViewCounts
}
