package com.bong.reminder.reminder.application.service

import com.bong.reminder.list.application.port.out.ReminderListRepositoryPort
import com.bong.reminder.list.domain.ReminderListNotFoundException
import com.bong.reminder.reminder.application.port.`in`.ReminderQueryService
import com.bong.reminder.reminder.application.port.out.ReminderRepositoryPort
import com.bong.reminder.reminder.application.query.ReminderView
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultReminderQueryService(
    private val reminderRepository: ReminderRepositoryPort,
    private val listRepository: ReminderListRepositoryPort,
) : ReminderQueryService {

    override fun findByList(listId: Long, completed: Boolean): List<ReminderView> {
        if (!listRepository.existsById(listId)) throw ReminderListNotFoundException()
        return reminderRepository.findByListIdOrdered(listId, completed).map(ReminderView::from)
    }
}
