package com.bong.reminder.list.adapter.out.persistence

import com.bong.reminder.config.JpaConfig
import com.bong.reminder.list.domain.ReminderList
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(JpaConfig::class, ReminderListPersistenceAdapter::class)
class ReminderListPersistenceAdapterTest @Autowired constructor(
    private val adapter: ReminderListPersistenceAdapter,
    private val jpaRepository: ReminderListJpaRepository,
) : DescribeSpec({

    extension(SpringExtension)

    beforeEach { jpaRepository.deleteAll() }

    describe("findAllOrdered") {
        it("sortOrder 오름차순으로 반환한다") {
            jpaRepository.save(ReminderList(name = "B", color = "#000000", sortOrder = 2))
            jpaRepository.save(ReminderList(name = "A", color = "#000000", sortOrder = 1))

            val result = adapter.findAllOrdered()

            result shouldHaveSize 2
            result[0].name shouldBe "A"
            result[1].name shouldBe "B"
        }
    }

    describe("findById") {
        it("존재하면 엔티티를 반환한다") {
            val saved = jpaRepository.save(ReminderList(name = "쇼핑", color = "#FF9500"))

            val found = adapter.findById(saved.id!!)

            found.shouldNotBeNull()
            found.name shouldBe "쇼핑"
        }

        it("존재하지 않으면 null 을 반환한다") {
            adapter.findById(999_999L).shouldBeNull()
        }
    }

    describe("save 시 createdAt / updatedAt 자동 채움") {
        it("저장 후 createdAt 과 updatedAt 이 설정된다") {
            val saved = adapter.save(ReminderList(name = "여행", color = "#0A84FF"))

            saved.createdAt.shouldNotBeNull()
            saved.updatedAt.shouldNotBeNull()
        }
    }
})
