package com.bong.reminder.list.adapter.out.persistence

import com.bong.reminder.list.domain.ReminderList
import org.springframework.data.jpa.repository.JpaRepository

interface ReminderListJpaRepository : JpaRepository<ReminderList, Long> {
    fun findAllByOrderBySortOrderAsc(): List<ReminderList>
}
