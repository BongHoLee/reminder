package com.bong.reminder.reminder.application.service

import com.bong.reminder.reminder.application.port.`in`.ReminderSearchService
import com.bong.reminder.reminder.application.port.out.ReminderRepositoryPort
import com.bong.reminder.reminder.application.query.ReminderView
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultReminderSearchService(
    private val reminderRepository: ReminderRepositoryPort,
) : ReminderSearchService {

    companion object {
        const val DEFAULT_LIMIT = 50
    }

    override fun search(query: String): List<ReminderView> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return reminderRepository.search(trimmed, DEFAULT_LIMIT).map(ReminderView::from)
    }
}
