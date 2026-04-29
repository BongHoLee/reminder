package com.bong.reminder.reminder.adapter.`in`.web.dto

import com.bong.reminder.reminder.domain.Priority
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.time.Instant

data class ReminderCreateRequest(
    @field:NotBlank(message = "미리 알림 제목은 비어 있을 수 없습니다.")
    @field:Size(max = 500, message = "미리 알림 제목은 500자 이하여야 합니다.")
    val title: String,

    @field:Size(max = 10_000, message = "메모는 10000자 이하여야 합니다.")
    val notes: String? = null,

    val dueAt: Instant? = null,

    val priority: Priority = Priority.NONE,

    val flagged: Boolean = false,

    @field:PositiveOrZero(message = "정렬 순서는 0 이상이어야 합니다.")
    val sortOrder: Int = 0,

    val parentId: Long? = null,
)
