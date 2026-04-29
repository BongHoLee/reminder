package com.bong.reminder.list.application.service

import com.bong.reminder.list.application.port.`in`.ReminderListQueryUseCase
import com.bong.reminder.list.application.port.out.ReminderListRepositoryPort
import com.bong.reminder.list.application.query.ReminderListView
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ReminderListQueryService(
    private val repository: ReminderListRepositoryPort,
) : ReminderListQueryUseCase {

    override fun findAll(): List<ReminderListView> =
        repository.findAllOrdered().map(ReminderListView::from)
}
