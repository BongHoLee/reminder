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

    override fun findByList(listId: Long, completed: Boolean?): List<ReminderView> {
        if (!listRepository.existsById(listId)) throw ReminderListNotFoundException()
        val rows = if (completed == null) {
            reminderRepository.findByListIdOrderedAll(listId)
        } else {
            reminderRepository.findByListIdOrdered(listId, completed)
        }
        return rows.map(ReminderView::from)
    }

    override fun findChildren(parentId: Long): List<ReminderView> {
        if (!reminderRepository.existsById(parentId)) {
            throw com.bong.reminder.reminder.domain.ReminderNotFoundException()
        }
        return reminderRepository.findByParentIdOrdered(parentId).map(ReminderView::from)
    }
}
