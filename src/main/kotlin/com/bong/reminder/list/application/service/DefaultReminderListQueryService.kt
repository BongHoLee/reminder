package com.bong.reminder.list.application.service

import com.bong.reminder.list.application.port.`in`.ReminderListQueryService
import com.bong.reminder.list.application.port.out.ReminderListRepositoryPort
import com.bong.reminder.list.application.query.ReminderListView
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultReminderListQueryService(
    private val repository: ReminderListRepositoryPort,
) : ReminderListQueryService {

    override fun findAll(): List<ReminderListView> =
        repository.findAllOrdered().map(ReminderListView::from)
}
