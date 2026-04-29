package com.bong.reminder.list.application.service

import com.bong.reminder.list.application.command.CreateReminderListCommand
import com.bong.reminder.list.application.command.DeleteReminderListCommand
import com.bong.reminder.list.application.command.UpdateReminderListCommand
import com.bong.reminder.list.application.port.`in`.ReminderListCommandService
import com.bong.reminder.list.application.port.out.ReminderListRepositoryPort
import com.bong.reminder.list.application.query.ReminderListView
import com.bong.reminder.list.domain.ReminderList
import com.bong.reminder.list.domain.ReminderListNotFoundException
import com.bong.reminder.reminder.application.port.out.ReminderRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DefaultReminderListCommandService(
    private val repository: ReminderListRepositoryPort,
    private val reminderRepository: ReminderRepositoryPort,
) : ReminderListCommandService {

    override fun create(command: CreateReminderListCommand): ReminderListView {
        val saved = repository.save(
            ReminderList(
                name = command.name,
                color = command.color,
                sortOrder = command.sortOrder,
            )
        )
        return ReminderListView.from(saved)
    }

    override fun update(command: UpdateReminderListCommand): ReminderListView {
        val entity = repository.findById(command.id) ?: throw ReminderListNotFoundException()
        command.name?.let(entity::rename)
        command.color?.let(entity::recolor)
        command.sortOrder?.let(entity::reorder)
        return ReminderListView.from(entity)
    }

    override fun delete(command: DeleteReminderListCommand) {
        if (!repository.existsById(command.id)) {
            throw ReminderListNotFoundException()
        }
        reminderRepository.deleteByListId(command.id)
        repository.deleteById(command.id)
    }
}
