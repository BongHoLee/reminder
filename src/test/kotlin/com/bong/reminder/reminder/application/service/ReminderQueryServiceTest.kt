package com.bong.reminder.reminder.application.service

import com.bong.reminder.list.adapter.out.persistence.ReminderListJpaRepository
import com.bong.reminder.list.domain.ReminderList
import com.bong.reminder.list.domain.ReminderListNotFoundException
import com.bong.reminder.reminder.adapter.out.persistence.ReminderJpaRepository
import com.bong.reminder.reminder.application.port.`in`.ReminderQueryService
import com.bong.reminder.reminder.domain.Reminder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReminderQueryServiceTest @Autowired constructor(
    private val service: ReminderQueryService,
    private val listJpaRepository: ReminderListJpaRepository,
    private val reminderJpaRepository: ReminderJpaRepository,
) : DescribeSpec({

    describe("findByList") {
        it("리스트의 미완료 reminder 를 sortOrder 오름차순으로 반환한다") {
            val list = listJpaRepository.save(ReminderList(name = "쇼핑", color = "#FF9500"))
            val a = reminderJpaRepository.save(Reminder(list = list, title = "A", sortOrder = 1))
            reminderJpaRepository.save(Reminder(list = list, title = "B", sortOrder = 0))
            // 완료된 항목은 제외 검증용
            val done = reminderJpaRepository.save(Reminder(list = list, title = "C", sortOrder = 2))
            done.toggleCompleted(java.time.Instant.now())
            reminderJpaRepository.flush()

            val result = service.findByList(list.id!!, completed = false)

            result shouldHaveSize 2
            result[0].title shouldBe "B"
            result[1].title shouldBe "A"
            result.map { it.id }.contains(a.id!!) shouldBe true
        }

        it("미존재 listId 는 ReminderListNotFoundException") {
            shouldThrow<ReminderListNotFoundException> {
                service.findByList(999_999L, completed = false)
            }
        }
    }
})
