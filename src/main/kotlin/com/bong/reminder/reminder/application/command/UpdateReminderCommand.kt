package com.bong.reminder.reminder.application.command

import com.bong.reminder.reminder.domain.Priority
import java.time.Instant

data class UpdateReminderCommand(
    val id: Long,
    val title: String? = null,
    val notes: String? = null,
    val notesClear: Boolean = false,
    val dueAt: Instant? = null,
    val dueAtClear: Boolean = false,
    val priority: Priority? = null,
    val flagged: Boolean? = null,
    val sortOrder: Int? = null,
    val parentId: Long? = null,
    val parentIdClear: Boolean = false,
) {
    init {
        require(!(notes != null && notesClear)) {
            "notes 와 notesClear 를 동시에 지정할 수 없습니다."
        }
        require(!(dueAt != null && dueAtClear)) {
            "dueAt 와 dueAtClear 를 동시에 지정할 수 없습니다."
        }
        require(!(parentId != null && parentIdClear)) {
            "parentId 와 parentIdClear 를 동시에 지정할 수 없습니다."
        }
    }
}
