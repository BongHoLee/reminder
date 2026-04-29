package com.bong.reminder.list.application.service

import com.bong.reminder.list.application.command.CreateReminderListCommand
import com.bong.reminder.list.application.command.DeleteReminderListCommand
import com.bong.reminder.list.application.command.UpdateReminderListCommand
import com.bong.reminder.list.application.port.out.ReminderListRepositoryPort
import com.bong.reminder.list.domain.ReminderList
import com.bong.reminder.list.domain.ReminderListNotFoundException
import com.bong.reminder.support.injectId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class ReminderListCommandServiceTest : DescribeSpec({

    val repository = mockk<ReminderListRepositoryPort>(relaxUnitFun = true)
    val service = ReminderListCommandService(repository)

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

    describe("create") {
        it("저장된 엔티티를 View 로 반환한다") {
            val captured = slot<ReminderList>()
            every { repository.save(capture(captured)) } answers {
                captured.captured.also { injectId(it, 10L) }
            }

            val view = service.create(
                CreateReminderListCommand(name = "여행", color = "#0A84FF", sortOrder = 2)
            )

            view.id shouldBe 10L
            view.name shouldBe "여행"
            view.color shouldBe "#0A84FF"
            view.sortOrder shouldBe 2
        }
    }

    describe("update") {
        it("null이 아닌 필드만 도메인 메서드로 적용한다") {
            val entity = persisted(1L, name = "쇼핑", color = "#FF9500", sortOrder = 0)
            every { repository.findById(1L) } returns entity

            val view = service.update(
                UpdateReminderListCommand(id = 1L, name = "장보기", color = null, sortOrder = 3),
            )

            view.name shouldBe "장보기"
            view.color shouldBe "#FF9500"
            view.sortOrder shouldBe 3
            entity.name shouldBe "장보기"
        }

        it("모든 필드가 null이면 변경하지 않는다") {
            val entity = persisted(1L)
            every { repository.findById(1L) } returns entity

            val view = service.update(UpdateReminderListCommand(id = 1L))

            view.name shouldBe "쇼핑"
            view.color shouldBe "#FF9500"
            view.sortOrder shouldBe 0
        }

        it("존재하지 않는 id는 ReminderListNotFoundException 을 던진다") {
            every { repository.findById(999L) } returns null

            val ex = shouldThrow<ReminderListNotFoundException> {
                service.update(UpdateReminderListCommand(id = 999L, name = "x"))
            }
            ex.message shouldBe "리스트를 찾을 수 없습니다."
        }

        it("도메인 검증 실패는 IllegalArgumentException 을 그대로 전파한다") {
            val entity = persisted(1L)
            every { repository.findById(1L) } returns entity

            shouldThrow<IllegalArgumentException> {
                service.update(UpdateReminderListCommand(id = 1L, name = "  "))
            }
        }
    }

    describe("delete") {
        it("존재하면 deleteById 를 호출한다") {
            every { repository.existsById(1L) } returns true

            service.delete(DeleteReminderListCommand(id = 1L))

            verify(exactly = 1) { repository.deleteById(1L) }
        }

        it("존재하지 않으면 ReminderListNotFoundException 을 던진다") {
            every { repository.existsById(999L) } returns false

            val ex = shouldThrow<ReminderListNotFoundException> {
                service.delete(DeleteReminderListCommand(id = 999L))
            }
            ex.message shouldBe "리스트를 찾을 수 없습니다."
            verify(exactly = 0) { repository.deleteById(any()) }
        }
    }
})
