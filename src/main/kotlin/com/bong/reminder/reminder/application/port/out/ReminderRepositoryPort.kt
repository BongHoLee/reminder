package com.bong.reminder.reminder.application.port.out

import com.bong.reminder.reminder.domain.Reminder

interface ReminderRepositoryPort {
    fun save(reminder: Reminder): Reminder
    fun findById(id: Long): Reminder?
    fun findByListIdOrdered(listId: Long, completed: Boolean): List<Reminder>
    fun findByParentIdOrdered(parentId: Long): List<Reminder>
    fun search(query: String, limit: Int): List<Reminder>

    fun findDueBetween(start: java.time.Instant, end: java.time.Instant, limit: Int): List<Reminder>
    fun findScheduledFrom(from: java.time.Instant): List<Reminder>
    fun findAllIncomplete(): List<Reminder>
    fun findFlagged(): List<Reminder>
    fun findCompleted(): List<Reminder>

    fun countDueBetween(start: java.time.Instant, end: java.time.Instant): Long
    fun countScheduledFrom(from: java.time.Instant): Long
    fun countAllIncomplete(): Long
    fun countFlagged(): Long
    fun countCompleted(): Long

    fun existsById(id: Long): Boolean
    fun deleteById(id: Long)
    fun deleteByListId(listId: Long)
}
