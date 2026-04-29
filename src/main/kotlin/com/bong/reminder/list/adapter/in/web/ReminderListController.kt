package com.bong.reminder.list.adapter.`in`.web

import com.bong.reminder.list.adapter.`in`.web.dto.ReminderListCreateRequest
import com.bong.reminder.list.adapter.`in`.web.dto.ReminderListResponse
import com.bong.reminder.list.adapter.`in`.web.dto.ReminderListUpdateRequest
import com.bong.reminder.list.application.command.DeleteReminderListCommand
import com.bong.reminder.list.application.port.`in`.ReminderListCommandService
import com.bong.reminder.list.application.port.`in`.ReminderListQueryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/lists")
class ReminderListController(
    private val commandService: ReminderListCommandService,
    private val queryService: ReminderListQueryService,
) {

    @GetMapping
    fun findAll(): List<ReminderListResponse> =
        queryService.findAll().map { it.toResponse() }

    @PostMapping
    fun create(
        @Valid @RequestBody request: ReminderListCreateRequest,
    ): ResponseEntity<ReminderListResponse> {
        val response = commandService.create(request.toCommand()).toResponse()
        return ResponseEntity
            .created(URI.create("/api/v1/lists/${response.id}"))
            .body(response)
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: ReminderListUpdateRequest,
    ): ReminderListResponse =
        commandService.update(request.toCommand(id)).toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        commandService.delete(DeleteReminderListCommand(id))
    }

}
