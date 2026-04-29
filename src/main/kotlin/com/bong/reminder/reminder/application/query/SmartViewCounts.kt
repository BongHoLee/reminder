package com.bong.reminder.reminder.application.query

data class SmartViewCounts(
    val today: Long,
    val scheduled: Long,
    val all: Long,
    val flagged: Long,
    val completed: Long,
)
