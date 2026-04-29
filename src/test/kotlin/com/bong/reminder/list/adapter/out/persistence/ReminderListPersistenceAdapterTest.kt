package com.bong.reminder.list.adapter.out.persistence

import com.bong.reminder.config.JpaConfig
import com.bong.reminder.list.domain.ReminderList
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(JpaConfig::class, ReminderListPersistenceAdapter::class)
class ReminderListPersistenceAdapterTest @Autowired constructor(
    private val adapter: ReminderListPersistenceAdapter,
    private val jpaRepository: ReminderListJpaRepository,
) : DescribeSpec({

    beforeEach { jpaRepository.deleteAll() }

    describe("findById — Optional → nullable 변환 (port contract)") {
        it("존재하지 않으면 null 을 반환한다") {
            adapter.findById(999_999L).shouldBeNull()
        }
    }

    describe("save — auditing 자동 채움") {
        it("저장 후 createdAt 과 updatedAt 이 설정된다") {
            val saved = adapter.save(ReminderList(name = "여행", color = "#0A84FF"))

            saved.createdAt.shouldNotBeNull()
            saved.updatedAt.shouldNotBeNull()
        }
    }
})
