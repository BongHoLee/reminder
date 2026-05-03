package com.bong.reminder.reminder.adapter.`in`.web

import com.bong.reminder.reminder.adapter.`in`.web.dto.ReminderResponse
import com.bong.reminder.reminder.application.port.`in`.ReminderViewQueryService
import com.bong.reminder.reminder.application.query.SmartViewCounts
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneId

@RestController
@RequestMapping("/api/v1/views")
class ReminderViewController(
    private val viewQueryService: ReminderViewQueryService,
) {

    @GetMapping("/today")
    fun today(
        @RequestParam(name = "tz", defaultValue = "UTC") tz: String,
        @RequestParam(name = "limit", defaultValue = "1000") limit: Int,
    ): List<ReminderResponse> =
        viewQueryService.today(ZoneId.of(tz), limit).map { it.toResponse() }

    @GetMapping("/scheduled")
    fun scheduled(@RequestParam(name = "tz", defaultValue = "UTC") tz: String): List<ReminderResponse> =
        viewQueryService.scheduled(ZoneId.of(tz)).map { it.toResponse() }

    @GetMapping("/all")
    fun all(): List<ReminderResponse> =
        viewQueryService.all().map { it.toResponse() }

    @GetMapping("/flagged")
    fun flagged(): List<ReminderResponse> =
        viewQueryService.flagged().map { it.toResponse() }

    @GetMapping("/completed")
    fun completed(): List<ReminderResponse> =
        viewQueryService.completed().map { it.toResponse() }

    @GetMapping("/counts")
    fun counts(@RequestParam(name = "tz", defaultValue = "UTC") tz: String): SmartViewCounts =
        viewQueryService.counts(ZoneId.of(tz))
}
