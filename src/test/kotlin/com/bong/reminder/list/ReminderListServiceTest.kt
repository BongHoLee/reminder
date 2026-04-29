package com.bong.reminder.list

import com.bong.reminder.list.domain.ReminderList
import com.bong.reminder.list.domain.ReminderListNotFoundException
import com.bong.reminder.list.dto.ReminderListCreateRequest
import com.bong.reminder.list.dto.ReminderListUpdateRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.lang.reflect.Field
import java.util.Optional

class ReminderListServiceTest : DescribeSpec({

    val repository = mockk<ReminderListRepository>(relaxUnitFun = true)
    val service = ReminderListService(repository)

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
            every { repository.findAllByOrderBySortOrderAsc() } returns listOf(
                persisted(1L, name = "쇼핑", sortOrder = 0),
                persisted(2L, name = "업무", sortOrder = 1),
            )

            val result = service.findAll()

            result shouldHaveSize 2
            result[0].name shouldBe "쇼핑"
            result[1].name shouldBe "업무"
        }
    }

    describe("create") {
        it("저장된 엔티티를 응답으로 반환한다") {
            val captured = slot<ReminderList>()
            every { repository.save(capture(captured)) } answers {
                captured.captured.also { injectId(it, 10L) }
            }

            val response = service.create(
                ReminderListCreateRequest(name = "여행", color = "#0A84FF", sortOrder = 2)
            )

            response.id shouldBe 10L
            response.name shouldBe "여행"
            response.color shouldBe "#0A84FF"
            response.sortOrder shouldBe 2
        }
    }

    describe("update") {
        it("null이 아닌 필드만 도메인 메서드로 적용한다") {
            val entity = persisted(1L, name = "쇼핑", color = "#FF9500", sortOrder = 0)
            every { repository.findById(1L) } returns Optional.of(entity)

            val response = service.update(
                1L,
                ReminderListUpdateRequest(name = "장보기", color = null, sortOrder = 3),
            )

            response.name shouldBe "장보기"
            response.color shouldBe "#FF9500"
            response.sortOrder shouldBe 3
            entity.name shouldBe "장보기"
        }

        it("모든 필드가 null이면 변경하지 않는다") {
            val entity = persisted(1L)
            every { repository.findById(1L) } returns Optional.of(entity)

            val response = service.update(1L, ReminderListUpdateRequest())

            response.name shouldBe "쇼핑"
            response.color shouldBe "#FF9500"
            response.sortOrder shouldBe 0
        }

        it("존재하지 않는 id는 ReminderListNotFoundException 을 던진다") {
            every { repository.findById(999L) } returns Optional.empty()

            val ex = shouldThrow<ReminderListNotFoundException> {
                service.update(999L, ReminderListUpdateRequest(name = "x"))
            }
            ex.message shouldBe "리스트를 찾을 수 없습니다."
        }

        it("도메인 검증 실패는 IllegalArgumentException 을 그대로 전파한다") {
            val entity = persisted(1L)
            every { repository.findById(1L) } returns Optional.of(entity)

            shouldThrow<IllegalArgumentException> {
                service.update(1L, ReminderListUpdateRequest(name = "  "))
            }
        }
    }

    describe("delete") {
        it("존재하면 deleteById 를 호출한다") {
            every { repository.existsById(1L) } returns true

            service.delete(1L)

            verify(exactly = 1) { repository.deleteById(1L) }
        }

        it("존재하지 않으면 ReminderListNotFoundException 을 던진다") {
            every { repository.existsById(999L) } returns false

            val ex = shouldThrow<ReminderListNotFoundException> {
                service.delete(999L)
            }
            ex.message shouldBe "리스트를 찾을 수 없습니다."
            verify(exactly = 0) { repository.deleteById(any()) }
        }
    }
})

private fun injectId(entity: Any, id: Long) {
    val field: Field = generateSequence<Class<*>>(entity::class.java) { it.superclass }
        .mapNotNull { runCatching { it.getDeclaredField("id") }.getOrNull() }
        .first()
    field.isAccessible = true
    field.set(entity, id)
}
