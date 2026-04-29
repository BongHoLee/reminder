package com.bong.reminder.list.application.port.out

import com.bong.reminder.list.domain.ReminderList

interface ReminderListRepositoryPort {
    fun save(list: ReminderList): ReminderList
    fun findById(id: Long): ReminderList?
    fun findAllOrdered(): List<ReminderList>
    fun existsById(id: Long): Boolean
    fun deleteById(id: Long)
}
