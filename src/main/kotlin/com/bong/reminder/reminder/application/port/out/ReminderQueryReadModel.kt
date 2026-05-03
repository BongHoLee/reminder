package com.bong.reminder.reminder.application.port.out

import com.bong.reminder.reminder.domain.Reminder
import java.time.Instant

interface ReminderQueryReadModel {
    fun findDueBetween(start: Instant, end: Instant, limit: Int): List<Reminder>
    fun findScheduledFrom(from: Instant): List<Reminder>
    fun findAllIncomplete(): List<Reminder>
    fun findFlagged(): List<Reminder>
    fun findCompleted(): List<Reminder>

    fun countDueBetween(start: Instant, end: Instant): Long
    fun countScheduledFrom(from: Instant): Long
    fun countAllIncomplete(): Long
    fun countFlagged(): Long
    fun countCompleted(): Long
}
