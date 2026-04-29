package com.bong.reminder.list.application.service

import com.bong.reminder.list.application.port.out.ReminderListRepositoryPort
import com.bong.reminder.list.domain.ReminderList
import com.bong.reminder.support.injectId
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk

class ReminderListQueryServiceTest : DescribeSpec({

    val repository = mockk<ReminderListRepositoryPort>()
    val service = ReminderListQueryService(repository)

    beforeEach { clearMocks(repository) }

    fun persisted(
        id: Long,
        name: String = "쇼핑",
        color: String = "#FF9500",
        sortOrder: Int = 0,
    ): ReminderList {
        val entity = ReminderList(name = name, color = color, sortOrder = sortOrder)
        injectId(entity, id)
        return entity
    }

    describe("findAll") {
        it("sortOrder 오름차순으로 응답을 반환한다") {
            every { repository.findAllOrdered() } returns listOf(
                persisted(1L, name = "쇼핑", sortOrder = 0),
                persisted(2L, name = "업무", sortOrder = 1),
            )

            val result = service.findAll()

            result shouldHaveSize 2
            result[0].name shouldBe "쇼핑"
            result[1].name shouldBe "업무"
        }

        it("저장된 리스트가 없으면 빈 리스트를 반환한다") {
            every { repository.findAllOrdered() } returns emptyList()

            service.findAll() shouldHaveSize 0
        }
    }
})
