package com.bong.reminder.reminder.application.service

import com.bong.reminder.reminder.application.port.`in`.ReminderViewQueryService
import com.bong.reminder.reminder.application.port.out.ReminderQueryReadModel
import com.bong.reminder.reminder.application.query.ReminderView
import com.bong.reminder.reminder.application.query.SmartViewCounts
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Service
@Transactional(readOnly = true)
class DefaultReminderViewQueryService(
    private val reminderRepository: ReminderQueryReadModel,
    private val clock: Clock,
) : ReminderViewQueryService {

    override fun today(zone: ZoneId, limit: Int): List<ReminderView> {
        val (start, end) = todayBoundary(zone)
        return reminderRepository.findDueBetween(start, end, limit).map(ReminderView::from)
    }

    override fun scheduled(zone: ZoneId): List<ReminderView> {
        val (_, end) = todayBoundary(zone)
        return reminderRepository.findScheduledFrom(end).map(ReminderView::from)
    }

    override fun all(): List<ReminderView> =
        reminderRepository.findAllIncomplete().map(ReminderView::from)

    override fun flagged(): List<ReminderView> =
        reminderRepository.findFlagged().map(ReminderView::from)

    override fun completed(): List<ReminderView> =
        reminderRepository.findCompleted().map(ReminderView::from)

    override fun counts(zone: ZoneId): SmartViewCounts {
        val (start, end) = todayBoundary(zone)
        return SmartViewCounts(
            today = reminderRepository.countDueBetween(start, end),
            scheduled = reminderRepository.countScheduledFrom(end),
            all = reminderRepository.countAllIncomplete(),
            flagged = reminderRepository.countFlagged(),
            completed = reminderRepository.countCompleted(),
        )
    }

    private fun todayBoundary(zone: ZoneId): Pair<Instant, Instant> {
        val today = LocalDate.now(clock.withZone(zone))
        val start = today.atStartOfDay(zone).toInstant()
        val end = today.plusDays(1).atStartOfDay(zone).toInstant()
        return start to end
    }
}
