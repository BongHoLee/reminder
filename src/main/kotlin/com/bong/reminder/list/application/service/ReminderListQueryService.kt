package com.bong.reminder.list.application.service

import com.bong.reminder.list.application.port.`in`.ReminderListQueryUseCase
import com.bong.reminder.list.application.port.out.ReminderListRepositoryPort
import com.bong.reminder.list.dto.ReminderListResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ReminderListQueryService(
    private val repository: ReminderListRepositoryPort,
) : ReminderListQueryUseCase {

    override fun findAll(): List<ReminderListResponse> =
        repository.findAllOrdered().map(ReminderListResponse::from)
}
