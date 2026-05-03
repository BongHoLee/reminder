package com.bong.reminder.reminder.application.port.out

import com.bong.reminder.reminder.domain.Reminder

interface ReminderRepositoryPort {
    fun save(reminder: Reminder): Reminder
    fun findById(id: Long): Reminder?
    fun findByListIdOrdered(listId: Long, completed: Boolean): List<Reminder>
    fun findByListIdOrderedAll(listId: Long): List<Reminder>
    fun findByParentIdOrdered(parentId: Long): List<Reminder>
    fun search(query: String, limit: Int): List<Reminder>

    fun existsById(id: Long): Boolean
    fun deleteById(id: Long)
    fun deleteByListId(listId: Long)
}
