package com.bong.reminder.list.application.service

import com.bong.reminder.list.application.port.`in`.ReminderListCommandUseCase
import com.bong.reminder.list.application.port.out.ReminderListRepositoryPort
import com.bong.reminder.list.domain.ReminderList
import com.bong.reminder.list.domain.ReminderListNotFoundException
import com.bong.reminder.list.dto.ReminderListCreateRequest
import com.bong.reminder.list.dto.ReminderListResponse
import com.bong.reminder.list.dto.ReminderListUpdateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ReminderListCommandService(
    private val repository: ReminderListRepositoryPort,
) : ReminderListCommandUseCase {

    override fun create(request: ReminderListCreateRequest): ReminderListResponse {
        val saved = repository.save(
            ReminderList(
                name = request.name,
                color = request.color,
                sortOrder = request.sortOrder,
            )
        )
        return ReminderListResponse.from(saved)
    }

    override fun update(id: Long, request: ReminderListUpdateRequest): ReminderListResponse {
        val entity = repository.findById(id) ?: throw ReminderListNotFoundException()
        request.name?.let(entity::rename)
        request.color?.let(entity::recolor)
        request.sortOrder?.let(entity::reorder)
        return ReminderListResponse.from(entity)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw ReminderListNotFoundException()
        }
        repository.deleteById(id)
    }
}
