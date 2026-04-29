package com.bong.reminder.list

import com.bong.reminder.list.domain.ReminderList
import com.bong.reminder.list.domain.ReminderListNotFoundException
import com.bong.reminder.list.dto.ReminderListCreateRequest
import com.bong.reminder.list.dto.ReminderListResponse
import com.bong.reminder.list.dto.ReminderListUpdateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ReminderListService(
    private val repository: ReminderListRepository,
) {

    @Transactional(readOnly = true)
    fun findAll(): List<ReminderListResponse> =
        repository.findAllByOrderBySortOrderAsc().map(ReminderListResponse::from)

    fun create(request: ReminderListCreateRequest): ReminderListResponse {
        val saved = repository.save(
            ReminderList(
                name = request.name,
                color = request.color,
                sortOrder = request.sortOrder,
            )
        )
        return ReminderListResponse.from(saved)
    }

    fun update(id: Long, request: ReminderListUpdateRequest): ReminderListResponse {
        val entity = findEntity(id)
        request.name?.let(entity::rename)
        request.color?.let(entity::recolor)
        request.sortOrder?.let(entity::reorder)
        return ReminderListResponse.from(entity)
    }

    fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw ReminderListNotFoundException()
        }
        repository.deleteById(id)
    }

    private fun findEntity(id: Long): ReminderList =
        repository.findById(id).orElseThrow {
            ReminderListNotFoundException()
        }
}
