package com.bong.reminder.reminder.adapter.`in`.web

import com.bong.reminder.reminder.adapter.`in`.web.dto.ReminderCreateRequest
import com.bong.reminder.reminder.adapter.`in`.web.dto.ReminderResponse
import com.bong.reminder.reminder.adapter.`in`.web.dto.ReminderUpdateRequest
import com.bong.reminder.reminder.application.command.DeleteReminderCommand
import com.bong.reminder.reminder.application.command.ToggleReminderCompletedCommand
import com.bong.reminder.reminder.application.port.`in`.ReminderCommandService
import com.bong.reminder.reminder.application.port.`in`.ReminderQueryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class ReminderController(
    private val commandService: ReminderCommandService,
    private val queryService: ReminderQueryService,
) {

    @GetMapping("/api/v1/lists/{listId}/reminders")
    fun findByList(
        @PathVariable listId: Long,
        @RequestParam(name = "completed", defaultValue = "false") completed: Boolean,
    ): List<ReminderResponse> =
        queryService.findByList(listId, completed).map { it.toResponse() }

    @PostMapping("/api/v1/lists/{listId}/reminders")
    fun create(
        @PathVariable listId: Long,
        @Valid @RequestBody request: ReminderCreateRequest,
    ): ResponseEntity<ReminderResponse> {
        val response = commandService.create(request.toCommand(listId)).toResponse()
        return ResponseEntity
            .created(URI.create("/api/v1/reminders/${response.id}"))
            .body(response)
    }

    @PatchMapping("/api/v1/reminders/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: ReminderUpdateRequest,
    ): ReminderResponse =
        commandService.update(request.toCommand(id)).toResponse()

    @PostMapping("/api/v1/reminders/{id}/toggle")
    fun toggle(@PathVariable id: Long): ReminderResponse =
        commandService.toggleCompleted(ToggleReminderCompletedCommand(id)).toResponse()

    @DeleteMapping("/api/v1/reminders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        commandService.delete(DeleteReminderCommand(id))
    }
}
