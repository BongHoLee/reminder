package com.bong.reminder.reminder.adapter.out.persistence

import com.bong.reminder.list.adapter.out.persistence.ReminderListJpaRepository
import com.bong.reminder.list.domain.ReminderList
import com.bong.reminder.reminder.domain.Reminder
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.hibernate.SessionFactory
import org.hibernate.stat.Statistics
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReminderLazyFetchTest @Autowired constructor(
    private val em: EntityManager,
    private val listJpaRepository: ReminderListJpaRepository,
    private val reminderJpaRepository: ReminderJpaRepository,
) : DescribeSpec({

    val sessionFactory = em.entityManagerFactory.unwrap(SessionFactory::class.java)
    val stats: Statistics = sessionFactory.statistics

    fun seed(): Long {
        val list = listJpaRepository.save(ReminderList(name = "쇼핑", color = "#FF9500"))
        repeat(5) {
            reminderJpaRepository.save(Reminder(list = list, title = "r$it"))
        }
        return list.id!!
    }

    describe("ManyToOne LAZY 프록시") {
        it("findByListId 후 entity.list.id 만 읽으면 추가 SELECT 가 발생하지 않는다") {
            val listId = seed()
            em.flush()
            em.clear()

            stats.clear()
            stats.isStatisticsEnabled = true

            val reminders = reminderJpaRepository.findByListIdAndCompletedOrderBySortOrderAsc(listId, false)
            // 결과 행 N개에 대해 list.id 만 접근 — 진짜 LAZY 라면 추가 SELECT 0회
            reminders.forEach { it.list.id }

            val queries = stats.prepareStatementCount
            // findBy 쿼리 1번 외에는 추가 SELECT 가 없어야 함 (id 는 FK 컬럼에서 직접 노출)
            queries shouldBe 1L
        }
    }
})
