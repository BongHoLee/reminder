package com.bong.reminder.reminder.adapter.out.persistence

import com.bong.reminder.reminder.domain.Reminder
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ReminderJpaRepository : JpaRepository<Reminder, Long> {

    fun findByListIdAndCompletedOrderBySortOrderAsc(listId: Long, completed: Boolean): List<Reminder>

    fun findByParentIdOrderBySortOrderAsc(parentId: Long): List<Reminder>

    @Query(
        """
        select r from Reminder r
        where lower(r.title) like lower(concat('%', :q, '%'))
           or lower(coalesce(r.notes, '')) like lower(concat('%', :q, '%'))
        order by r.updatedAt desc
        """,
    )
    fun searchByTitleOrNotes(@Param("q") query: String, pageable: Pageable): List<Reminder>

    fun findByCompletedFalseAndDueAtGreaterThanEqualAndDueAtLessThanOrderByDueAtAsc(
        start: java.time.Instant,
        end: java.time.Instant,
    ): List<Reminder>

    fun findByCompletedFalseAndDueAtGreaterThanEqualOrderByDueAtAsc(from: java.time.Instant): List<Reminder>

    fun findByCompletedFalseOrderByDueAtAscSortOrderAsc(): List<Reminder>

    fun findByFlaggedTrueOrderBySortOrderAsc(): List<Reminder>

    fun findByCompletedTrueOrderByCompletedAtDesc(): List<Reminder>

    fun countByCompletedFalseAndDueAtGreaterThanEqualAndDueAtLessThan(
        start: java.time.Instant,
        end: java.time.Instant,
    ): Long

    fun countByCompletedFalseAndDueAtGreaterThanEqual(from: java.time.Instant): Long

    fun countByCompletedFalse(): Long

    fun countByFlaggedTrue(): Long

    fun countByCompletedTrue(): Long

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Reminder r where r.list.id = :listId")
    fun deleteByListId(listId: Long)
}
