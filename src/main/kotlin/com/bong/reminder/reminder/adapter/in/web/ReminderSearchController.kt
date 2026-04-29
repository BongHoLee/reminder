package com.bong.reminder.reminder.adapter.`in`.web

import com.bong.reminder.reminder.adapter.`in`.web.dto.ReminderResponse
import com.bong.reminder.reminder.application.port.`in`.ReminderSearchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ReminderSearchController(
    private val searchService: ReminderSearchService,
) {

    @GetMapping("/api/v1/search")
    fun search(@RequestParam("q") query: String): List<ReminderResponse> =
        searchService.search(query).map { it.toResponse() }
}
