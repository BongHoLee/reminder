package com.bong.reminder.list.application.service

import com.bong.reminder.list.adapter.out.persistence.ReminderListJpaRepository
import com.bong.reminder.list.application.command.CreateReminderListCommand
import com.bong.reminder.list.application.command.DeleteReminderListCommand
import com.bong.reminder.list.application.command.UpdateReminderListCommand
import com.bong.reminder.list.application.port.`in`.ReminderListCommandService
import com.bong.reminder.list.domain.ReminderListNotFoundException
import com.bong.reminder.reminder.adapter.out.persistence.ReminderJpaRepository
import com.bong.reminder.reminder.domain.Reminder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReminderListCommandServiceTest @Autowired constructor(
    private val service: ReminderListCommandService,
    private val jpaRepository: ReminderListJpaRepository,
    private val reminderJpaRepository: ReminderJpaRepository,
) : DescribeSpec({

    describe("create") {
        it("DB 에 row 가 생성되고 auditing 시각이 채워진다") {
            val view = service.create(
                CreateReminderListCommand(name = "여행", color = "#0A84FF", sortOrder = 2)
            )

            view.name shouldBe "여행"
            view.color shouldBe "#0A84FF"
            view.sortOrder shouldBe 2
            view.createdAt shouldNotBe Instant.EPOCH
            view.updatedAt shouldNotBe Instant.EPOCH

            jpaRepository.count() shouldBe 1L
            val saved = jpaRepository.findById(view.id).orElseThrow()
            saved.name shouldBe "여행"
            saved.color shouldBe "#0A84FF"
            saved.sortOrder shouldBe 2
        }
    }

    describe("update") {
        it("null 이 아닌 필드만 적용되고 dirty checking 으로 DB 에 반영된다") {
            val created = service.create(
                CreateReminderListCommand(name = "쇼핑", color = "#FF9500", sortOrder = 0)
            )

            val view = service.update(
                UpdateReminderListCommand(id = created.id, name = "장보기", color = null, sortOrder = 3),
            )

            view.name shouldBe "장보기"
            view.color shouldBe "#FF9500"
            view.sortOrder shouldBe 3
            view.updatedAt shouldBeGreaterThanOrEqualTo created.updatedAt

            val refetched = jpaRepository.findById(created.id).orElseThrow()
            refetched.name shouldBe "장보기"
            refetched.color shouldBe "#FF9500"
            refetched.sortOrder shouldBe 3
        }

        it("모든 필드가 null 이면 DB 변경 없음") {
            val created = service.create(
                CreateReminderListCommand(name = "쇼핑", color = "#FF9500", sortOrder = 0)
            )

            service.update(UpdateReminderListCommand(id = created.id))

            val refetched = jpaRepository.findById(created.id).orElseThrow()
            refetched.name shouldBe "쇼핑"
            refetched.color shouldBe "#FF9500"
            refetched.sortOrder shouldBe 0
        }

        it("미존재 id 는 ReminderListNotFoundException 을 던지고 DB 에 영향이 없다") {
            val ex = shouldThrow<ReminderListNotFoundException> {
                service.update(UpdateReminderListCommand(id = 999_999L, name = "x"))
            }
            ex.message shouldBe "리스트를 찾을 수 없습니다."
            jpaRepository.count() shouldBe 0L
        }

        it("도메인 검증 실패는 IllegalArgumentException 을 그대로 전파한다") {
            val created = service.create(
                CreateReminderListCommand(name = "원본", color = "#FF9500", sortOrder = 1)
            )

            shouldThrow<IllegalArgumentException> {
                service.update(UpdateReminderListCommand(id = created.id, name = "  "))
            }
        }
    }

    describe("delete") {
        it("존재하면 DB row 를 제거한다") {
            val created = service.create(
                CreateReminderListCommand(name = "쇼핑", color = "#FF9500", sortOrder = 0)
            )

            service.delete(DeleteReminderListCommand(id = created.id))

            jpaRepository.count() shouldBe 0L
        }

        it("미존재 id 는 ReminderListNotFoundException 을 던지고 다른 row 에 영향이 없다") {
            val created = service.create(
                CreateReminderListCommand(name = "쇼핑", color = "#FF9500", sortOrder = 0)
            )

            shouldThrow<ReminderListNotFoundException> {
                service.delete(DeleteReminderListCommand(id = 999_999L))
            }

            jpaRepository.count() shouldBe 1L
            jpaRepository.findById(created.id).orElse(null).shouldNotBeNull()
        }

        it("리스트 삭제 시 그 리스트의 reminder 와 자식 reminder 까지 cascade 로 삭제된다") {
            val list = service.create(
                CreateReminderListCommand(name = "삭제대상", color = "#FF3B30", sortOrder = 0)
            )
            val listEntity = jpaRepository.findById(list.id).orElseThrow()
            val parent = reminderJpaRepository.save(
                Reminder(list = listEntity, title = "부모"),
            )
            reminderJpaRepository.save(
                Reminder(list = listEntity, title = "자식", parent = parent),
            )

            val other = service.create(
                CreateReminderListCommand(name = "유지", color = "#0A84FF", sortOrder = 1)
            )
            val otherEntity = jpaRepository.findById(other.id).orElseThrow()
            reminderJpaRepository.save(Reminder(list = otherEntity, title = "유지대상"))

            service.delete(DeleteReminderListCommand(id = list.id))

            reminderJpaRepository.count() shouldBe 1L
            reminderJpaRepository.findAll().single().title shouldBe "유지대상"
            jpaRepository.count() shouldBe 1L
        }
    }
})
