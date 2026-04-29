package com.bong.reminder.reminder.adapter.`in`.web.dto

import com.bong.reminder.reminder.domain.Priority
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.time.Instant

data class ReminderUpdateRequest(
    @field:Size(max = 500, message = "미리 알림 제목은 500자 이하여야 합니다.")
    val title: String? = null,

    @field:Size(max = 10_000, message = "메모는 10000자 이하여야 합니다.")
    val notes: String? = null,
    val notesClear: Boolean = false,

    val dueAt: Instant? = null,
    val dueAtClear: Boolean = false,

    val priority: Priority? = null,

    val flagged: Boolean? = null,

    @field:PositiveOrZero(message = "정렬 순서는 0 이상이어야 합니다.")
    val sortOrder: Int? = null,

    val parentId: Long? = null,
    val parentIdClear: Boolean = false,
)
