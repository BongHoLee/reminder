package com.bong.reminder.reminder.application.query

import com.bong.reminder.reminder.domain.Priority
import com.bong.reminder.reminder.domain.Reminder
import java.time.Instant

data class ReminderView(
    val id: Long,
    val listId: Long,
    val parentId: Long?,
    val title: String,
    val notes: String?,
    val dueAt: Instant?,
    val priority: Priority,
    val completed: Boolean,
    val completedAt: Instant?,
    val flagged: Boolean,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(entity: Reminder): ReminderView = ReminderView(
            id = requireNotNull(entity.id) { "저장되지 않은 미리 알림은 응답으로 변환할 수 없습니다." },
            listId = requireNotNull(entity.list.id) { "리스트 id 가 비어 있습니다." },
            parentId = entity.parent?.id,
            title = entity.title,
            notes = entity.notes,
            dueAt = entity.dueAt,
            priority = entity.priority,
            completed = entity.completed,
            completedAt = entity.completedAt,
            flagged = entity.flagged,
            sortOrder = entity.sortOrder,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}
