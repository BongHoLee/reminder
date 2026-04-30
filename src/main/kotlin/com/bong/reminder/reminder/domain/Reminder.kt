package com.bong.reminder.reminder.domain

import com.bong.reminder.common.BaseEntity
import com.bong.reminder.list.domain.ReminderList
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "reminder",
    indexes = [
        Index(name = "idx_reminder_list_completed", columnList = "list_id,completed"),
        Index(name = "idx_reminder_due_at", columnList = "due_at"),
        Index(name = "idx_reminder_parent", columnList = "parent_id"),
        Index(name = "idx_reminder_completed", columnList = "completed"),
        Index(name = "idx_reminder_completed_completed_at", columnList = "completed,completed_at"),
    ],
)
class Reminder(
    list: ReminderList,
    title: String,
    notes: String? = null,
    dueAt: Instant? = null,
    priority: Priority = Priority.NONE,
    flagged: Boolean = false,
    sortOrder: Int = 0,
    parent: Reminder? = null,
) : BaseEntity() {
    init {
        validateTitle(title)
        validateParent(parent)
        require(sortOrder >= 0) { "정렬 순서는 0 이상이어야 합니다." }
    }

    companion object {
        private fun validateTitle(title: String) {
            require(title.isNotBlank()) { "미리 알림 제목은 비어 있을 수 없습니다." }
            require(title.length <= 500) { "미리 알림 제목은 500자 이하여야 합니다." }
        }

        private fun validateNotes(notes: String?) {
            if (notes == null) return
            require(notes.length <= 10_000) { "메모는 10000자 이하여야 합니다." }
        }

        private fun validateParent(parent: Reminder?) {
            if (parent == null) return
            require(parent.parent == null) { "하위 작업은 1단계 깊이까지만 허용됩니다." }
        }
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "list_id", nullable = false)
    var list: ReminderList = list
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Reminder? = parent
        protected set

    @Column(nullable = false, length = 500)
    var title: String = title
        protected set

    @Column(columnDefinition = "TEXT")
    var notes: String? = notes
        protected set

    @Column(name = "due_at")
    var dueAt: Instant? = dueAt
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var priority: Priority = priority
        protected set

    @Column(nullable = false)
    var completed: Boolean = false
        protected set

    @Column(name = "completed_at")
    var completedAt: Instant? = null
        protected set

    @Column(nullable = false)
    var flagged: Boolean = flagged
        protected set

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = sortOrder
        protected set

    fun rename(newTitle: String) {
        validateTitle(newTitle)
        this.title = newTitle
    }

    fun changeNotes(newNotes: String?) {
        validateNotes(newNotes)
        this.notes = newNotes
    }

    fun changeDueAt(newDueAt: Instant?) {
        this.dueAt = newDueAt
    }

    fun changePriority(newPriority: Priority) {
        this.priority = newPriority
    }

    fun changeFlagged(newFlagged: Boolean) {
        this.flagged = newFlagged
    }

    fun reorder(newSortOrder: Int) {
        require(newSortOrder >= 0) { "정렬 순서는 0 이상이어야 합니다." }
        this.sortOrder = newSortOrder
    }

    fun changeParent(newParent: Reminder?) {
        if (newParent != null) {
            require(newParent !== this) { "자기 자신을 상위 작업으로 지정할 수 없습니다." }
            require(newParent.parent == null) { "하위 작업은 1단계 깊이까지만 허용됩니다." }
        }
        this.parent = newParent
    }

    fun toggleCompleted(now: Instant) {
        if (completed) {
            completed = false
            completedAt = null
        } else {
            completed = true
            completedAt = now
        }
    }
}
