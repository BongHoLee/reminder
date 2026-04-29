package com.bong.reminder.reminder.adapter.out.persistence

import com.bong.reminder.reminder.application.port.out.ReminderRepositoryPort
import com.bong.reminder.reminder.domain.Reminder
import org.springframework.stereotype.Component

@Component
class ReminderPersistenceAdapter(
    private val jpaRepository: ReminderJpaRepository,
) : ReminderRepositoryPort {

    override fun save(reminder: Reminder): Reminder = jpaRepository.save(reminder)

    override fun findById(id: Long): Reminder? = jpaRepository.findById(id).orElse(null)

    override fun findByListIdOrdered(listId: Long, completed: Boolean): List<Reminder> =
        jpaRepository.findByListIdAndCompletedOrderBySortOrderAsc(listId, completed)

    override fun findByParentIdOrdered(parentId: Long): List<Reminder> =
        jpaRepository.findByParentIdOrderBySortOrderAsc(parentId)

    override fun existsById(id: Long): Boolean = jpaRepository.existsById(id)

    override fun deleteById(id: Long) {
        jpaRepository.deleteById(id)
    }

    override fun deleteByListId(listId: Long) {
        jpaRepository.deleteByListId(listId)
    }
}
