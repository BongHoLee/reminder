package com.bong.reminder

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReminderApplication

fun main(args: Array<String>) {
	runApplication<ReminderApplication>(*args)
}
