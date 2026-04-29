package com.bong.reminder.list

import com.bong.reminder.list.domain.ReminderList
import org.springframework.data.jpa.repository.JpaRepository

interface ReminderListRepository : JpaRepository<ReminderList, Long> {
    fun findAllByOrderBySortOrderAsc(): List<ReminderList>
}
