package com.bong.reminder.list.application.query

import com.bong.reminder.list.domain.ReminderList
import java.time.Instant

data class ReminderListView(
    val id: Long,
    val name: String,
    val color: String,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(entity: ReminderList): ReminderListView = ReminderListView(
            id = requireNotNull(entity.id) { "저장되지 않은 리스트는 응답으로 변환할 수 없습니다." },
            name = entity.name,
            color = entity.color,
            sortOrder = entity.sortOrder,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}
