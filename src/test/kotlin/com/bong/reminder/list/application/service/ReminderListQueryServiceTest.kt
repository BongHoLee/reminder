package com.bong.reminder.list.application.service

import com.bong.reminder.config.JpaConfig
import com.bong.reminder.list.adapter.out.persistence.ReminderListJpaRepository
import com.bong.reminder.list.adapter.out.persistence.ReminderListPersistenceAdapter
import com.bong.reminder.list.application.port.`in`.ReminderListQueryService
import com.bong.reminder.list.domain.ReminderList
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(JpaConfig::class, ReminderListPersistenceAdapter::class, DefaultReminderListQueryService::class)
class ReminderListQueryServiceTest @Autowired constructor(
    private val service: ReminderListQueryService,
    private val jpaRepository: ReminderListJpaRepository,
) : DescribeSpec({

    afterEach { jpaRepository.deleteAll() }

    describe("findAll") {
        it("sortOrder 오름차순으로 반환한다") {
            jpaRepository.save(ReminderList(name = "B", color = "#000000", sortOrder = 2))
            jpaRepository.save(ReminderList(name = "A", color = "#000000", sortOrder = 1))

            val result = service.findAll()

            result shouldHaveSize 2
            result[0].name shouldBe "A"
            result[1].name shouldBe "B"
        }

        it("저장된 리스트가 없으면 빈 리스트를 반환한다") {
            service.findAll() shouldHaveSize 0
        }
    }
})
