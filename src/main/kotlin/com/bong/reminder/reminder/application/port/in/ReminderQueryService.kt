package com.bong.reminder.reminder.application.port.`in`

import com.bong.reminder.reminder.application.query.ReminderView

interface ReminderQueryService {
    /** completed 가 null 이면 완료/미완료 모두 반환 (sortOrder 오름차순). */
    fun findByList(listId: Long, completed: Boolean?): List<ReminderView>
    fun findChildren(parentId: Long): List<ReminderView>
}
