package com.bong.reminder.reminder.adapter.out.persistence

import com.bong.reminder.reminder.domain.Reminder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface ReminderJpaRepository : JpaRepository<Reminder, Long> {

    fun findByListIdAndCompletedOrderBySortOrderAsc(listId: Long, completed: Boolean): List<Reminder>

    fun findByParentIdOrderBySortOrderAsc(parentId: Long): List<Reminder>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Reminder r where r.list.id = :listId")
    fun deleteByListId(listId: Long)
}
