package com.bong.reminder.reminder.application.service

import com.bong.reminder.list.application.port.out.ReminderListRepositoryPort
import com.bong.reminder.list.domain.ReminderListNotFoundException
import com.bong.reminder.reminder.application.command.CreateReminderCommand
import com.bong.reminder.reminder.application.command.DeleteReminderCommand
import com.bong.reminder.reminder.application.command.ToggleReminderCompletedCommand
import com.bong.reminder.reminder.application.command.UpdateReminderCommand
import com.bong.reminder.reminder.application.port.`in`.ReminderCommandService
import com.bong.reminder.reminder.application.port.out.ReminderRepositoryPort
import com.bong.reminder.reminder.application.query.ReminderView
import com.bong.reminder.reminder.domain.Reminder
import com.bong.reminder.reminder.domain.ReminderNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
@Transactional
class DefaultReminderCommandService(
    private val reminderRepository: ReminderRepositoryPort,
    private val listRepository: ReminderListRepositoryPort,
    private val clock: Clock,
) : ReminderCommandService {

    override fun create(command: CreateReminderCommand): ReminderView {
        val list = listRepository.findById(command.listId) ?: throw ReminderListNotFoundException()
        val parent = command.parentId?.let {
            reminderRepository.findById(it) ?: throw ReminderNotFoundException()
        }
        val saved = reminderRepository.save(
            Reminder(
                list = list,
                title = command.title,
                notes = command.notes,
                dueAt = command.dueAt,
                priority = command.priority,
                flagged = command.flagged,
                sortOrder = command.sortOrder,
                parent = parent,
            ),
        )
        return ReminderView.from(saved)
    }

    override fun update(command: UpdateReminderCommand): ReminderView {
        val entity = reminderRepository.findById(command.id) ?: throw ReminderNotFoundException()

        command.title?.let(entity::rename)
        when {
            command.notesClear -> entity.changeNotes(null)
            command.notes != null -> entity.changeNotes(command.notes)
        }
        when {
            command.dueAtClear -> entity.changeDueAt(null)
            command.dueAt != null -> entity.changeDueAt(command.dueAt)
        }
        command.priority?.let(entity::changePriority)
        command.flagged?.let(entity::changeFlagged)
        command.sortOrder?.let(entity::reorder)
        when {
            command.parentIdClear -> entity.changeParent(null)
            command.parentId != null -> {
                val newParent = reminderRepository.findById(command.parentId) ?: throw ReminderNotFoundException()
                entity.changeParent(newParent)
            }
        }

        return ReminderView.from(entity)
    }

    override fun toggleCompleted(command: ToggleReminderCompletedCommand): ReminderView {
        val entity = reminderRepository.findById(command.id) ?: throw ReminderNotFoundException()
        entity.toggleCompleted(Instant.now(clock))
        return ReminderView.from(entity)
    }

    override fun delete(command: DeleteReminderCommand) {
        if (!reminderRepository.existsById(command.id)) {
            throw ReminderNotFoundException()
        }
        reminderRepository.deleteById(command.id)
    }
}
