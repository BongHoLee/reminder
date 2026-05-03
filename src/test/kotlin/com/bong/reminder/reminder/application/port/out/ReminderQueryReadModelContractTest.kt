package com.bong.reminder.reminder.application.port.out

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll

class ReminderQueryReadModelContractTest : DescribeSpec({
    describe("ReminderQueryReadModel 인터페이스") {
        it("존재해야 하고 view + count 10개 메서드를 보유한다") {
            val cls = Class.forName(
                "com.bong.reminder.reminder.application.port.out.ReminderQueryReadModel",
            )
            val methodNames = cls.declaredMethods.map { it.name }.toSet()
            methodNames shouldContainAll setOf(
                "findDueBetween",
                "findScheduledFrom",
                "findAllIncomplete",
                "findFlagged",
                "findCompleted",
                "countDueBetween",
                "countScheduledFrom",
                "countAllIncomplete",
                "countFlagged",
                "countCompleted",
            )
        }
    }
})
