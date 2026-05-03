package com.bong.reminder.reminder.adapter.out.persistence

import com.bong.reminder.reminder.application.port.out.ReminderQueryReadModel
import com.bong.reminder.reminder.application.port.out.ReminderRepositoryPort
import com.bong.reminder.reminder.domain.Reminder
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ReminderPersistenceAdapter(
    private val jpaRepository: ReminderJpaRepository,
) : ReminderRepositoryPort, ReminderQueryReadModel {

    override fun save(reminder: Reminder): Reminder = jpaRepository.save(reminder)

    override fun findById(id: Long): Reminder? = jpaRepository.findById(id).orElse(null)

    override fun findByListIdOrdered(listId: Long, completed: Boolean): List<Reminder> =
        jpaRepository.findByListIdAndCompletedOrderBySortOrderAsc(listId, completed)

    override fun findByListIdOrderedAll(listId: Long): List<Reminder> =
        jpaRepository.findByListIdOrderBySortOrderAsc(listId)

    override fun findByParentIdOrdered(parentId: Long): List<Reminder> =
        jpaRepository.findByParentIdOrderBySortOrderAsc(parentId)

    override fun search(query: String, limit: Int): List<Reminder> =
        jpaRepository.searchByTitleOrNotes(query, PageRequest.of(0, limit))

    override fun findDueBetween(start: Instant, end: Instant, limit: Int): List<Reminder> =
        jpaRepository.findByCompletedFalseAndDueAtGreaterThanEqualAndDueAtLessThanOrderByDueAtAsc(
            start,
            end,
            PageRequest.of(0, limit),
        )

    override fun findScheduledFrom(from: Instant): List<Reminder> =
        jpaRepository.findByCompletedFalseAndDueAtGreaterThanEqualOrderByDueAtAsc(from)

    override fun findAllIncomplete(): List<Reminder> =
        jpaRepository.findByCompletedFalseOrderByDueAtAscSortOrderAsc()

    override fun findFlagged(): List<Reminder> =
        jpaRepository.findByFlaggedTrueOrderBySortOrderAsc()

    override fun findCompleted(): List<Reminder> =
        jpaRepository.findByCompletedTrueOrderByCompletedAtDesc()

    override fun countDueBetween(start: Instant, end: Instant): Long =
        jpaRepository.countByCompletedFalseAndDueAtGreaterThanEqualAndDueAtLessThan(start, end)

    override fun countScheduledFrom(from: Instant): Long =
        jpaRepository.countByCompletedFalseAndDueAtGreaterThanEqual(from)

    override fun countAllIncomplete(): Long = jpaRepository.countByCompletedFalse()

    override fun countFlagged(): Long = jpaRepository.countByFlaggedTrue()

    override fun countCompleted(): Long = jpaRepository.countByCompletedTrue()

    override fun existsById(id: Long): Boolean = jpaRepository.existsById(id)

    override fun deleteById(id: Long) {
        // 자식 → 부모 순서로 bulk delete + PC clear. FK CASCADE 에 의존하지 않는 코드 일관 정책.
        jpaRepository.deleteByParentId(id)
        jpaRepository.deleteByIdBulk(id)
    }

    override fun deleteByListId(listId: Long) {
        // 자식 → 부모 순서로 bulk delete + PC clear (FK CASCADE 미사용).
        jpaRepository.deleteChildrenByListId(listId)
        jpaRepository.deleteByListId(listId)
    }
}
