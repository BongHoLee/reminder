package com.bong.reminder.reminder.adapter.out.persistence

import com.bong.reminder.reminder.application.port.out.ReminderRepositoryPort
import com.bong.reminder.reminder.domain.Reminder
import org.springframework.data.domain.PageRequest
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

    override fun search(query: String, limit: Int): List<Reminder> =
        jpaRepository.searchByTitleOrNotes(query, PageRequest.of(0, limit))

    override fun findDueBetween(start: java.time.Instant, end: java.time.Instant): List<Reminder> =
        jpaRepository.findByCompletedFalseAndDueAtGreaterThanEqualAndDueAtLessThanOrderByDueAtAsc(start, end)

    override fun findScheduledFrom(from: java.time.Instant): List<Reminder> =
        jpaRepository.findByCompletedFalseAndDueAtGreaterThanEqualOrderByDueAtAsc(from)

    override fun findAllIncomplete(): List<Reminder> =
        jpaRepository.findByCompletedFalseOrderByDueAtAscSortOrderAsc()

    override fun findFlagged(): List<Reminder> =
        jpaRepository.findByFlaggedTrueOrderBySortOrderAsc()

    override fun findCompleted(): List<Reminder> =
        jpaRepository.findByCompletedTrueOrderByCompletedAtDesc()

    override fun countDueBetween(start: java.time.Instant, end: java.time.Instant): Long =
        jpaRepository.countByCompletedFalseAndDueAtGreaterThanEqualAndDueAtLessThan(start, end)

    override fun countScheduledFrom(from: java.time.Instant): Long =
        jpaRepository.countByCompletedFalseAndDueAtGreaterThanEqual(from)

    override fun countAllIncomplete(): Long = jpaRepository.countByCompletedFalse()

    override fun countFlagged(): Long = jpaRepository.countByFlaggedTrue()

    override fun countCompleted(): Long = jpaRepository.countByCompletedTrue()

    override fun existsById(id: Long): Boolean = jpaRepository.existsById(id)

    override fun deleteById(id: Long) {
        jpaRepository.deleteById(id)
    }

    override fun deleteByListId(listId: Long) {
        jpaRepository.deleteByListId(listId)
    }
}
