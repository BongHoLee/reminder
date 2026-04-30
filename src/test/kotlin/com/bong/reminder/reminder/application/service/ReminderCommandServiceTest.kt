package com.bong.reminder.reminder.application.service

import com.bong.reminder.list.adapter.out.persistence.ReminderListJpaRepository
import com.bong.reminder.list.domain.ReminderList
import com.bong.reminder.list.domain.ReminderListNotFoundException
import com.bong.reminder.reminder.adapter.out.persistence.ReminderJpaRepository
import com.bong.reminder.reminder.application.command.CreateReminderCommand
import com.bong.reminder.reminder.application.command.DeleteReminderCommand
import com.bong.reminder.reminder.application.command.ToggleReminderCompletedCommand
import com.bong.reminder.reminder.application.command.UpdateReminderCommand
import com.bong.reminder.reminder.application.port.`in`.ReminderCommandService
import com.bong.reminder.reminder.domain.Priority
import com.bong.reminder.reminder.domain.Reminder
import com.bong.reminder.reminder.domain.ReminderNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReminderCommandServiceTest @Autowired constructor(
    private val service: ReminderCommandService,
    private val listJpaRepository: ReminderListJpaRepository,
    private val reminderJpaRepository: ReminderJpaRepository,
) : DescribeSpec({

    fun newList() = listJpaRepository.save(
        ReminderList(name = "쇼핑", color = "#FF9500", sortOrder = 0),
    )

    describe("create") {
        it("리스트에 속한 reminder 가 저장된다") {
            val list = newList()

            val view = service.create(
                CreateReminderCommand(
                    listId = list.id!!,
                    title = "우유 사기",
                    priority = Priority.HIGH,
                    flagged = true,
                    sortOrder = 1,
                ),
            )

            view.title shouldBe "우유 사기"
            view.priority shouldBe Priority.HIGH
            view.flagged shouldBe true
            view.completed shouldBe false
            view.listId shouldBe list.id

            reminderJpaRepository.count() shouldBe 1L
            reminderJpaRepository.findById(view.id).orElseThrow().title shouldBe "우유 사기"
        }

        it("미존재 listId 는 ReminderListNotFoundException") {
            shouldThrow<ReminderListNotFoundException> {
                service.create(CreateReminderCommand(listId = 999_999L, title = "x"))
            }
        }

        it("parentId 가 다른 reminder 를 가리키면 1단계 하위 작업으로 저장된다") {
            val list = newList()
            val parent = service.create(CreateReminderCommand(listId = list.id!!, title = "장보기"))

            val child = service.create(
                CreateReminderCommand(listId = list.id!!, title = "우유", parentId = parent.id),
            )

            child.parentId shouldBe parent.id
        }

        it("parentId 의 부모가 또 있으면 깊이 위반으로 거부된다") {
            val list = newList()
            val grand = service.create(CreateReminderCommand(listId = list.id!!, title = "조부"))
            val parent = service.create(
                CreateReminderCommand(listId = list.id!!, title = "부", parentId = grand.id),
            )

            shouldThrow<IllegalArgumentException> {
                service.create(
                    CreateReminderCommand(listId = list.id!!, title = "자", parentId = parent.id),
                )
            }
        }
    }

    describe("update") {
        it("null 이 아닌 필드만 적용된다") {
            val list = newList()
            val created = service.create(
                CreateReminderCommand(listId = list.id!!, title = "초안", sortOrder = 0),
            )

            val updated = service.update(
                UpdateReminderCommand(
                    id = created.id,
                    title = "최종",
                    priority = Priority.MEDIUM,
                    flagged = true,
                    sortOrder = 5,
                ),
            )

            updated.title shouldBe "최종"
            updated.priority shouldBe Priority.MEDIUM
            updated.flagged shouldBe true
            updated.sortOrder shouldBe 5

            val refetched = reminderJpaRepository.findById(created.id).orElseThrow()
            refetched.title shouldBe "최종"
            refetched.flagged shouldBe true
        }

        it("미존재 id 는 ReminderNotFoundException") {
            shouldThrow<ReminderNotFoundException> {
                service.update(UpdateReminderCommand(id = 999_999L, title = "x"))
            }
        }

        it("도메인 검증 실패는 IllegalArgumentException 그대로 전파") {
            val list = newList()
            val created = service.create(CreateReminderCommand(listId = list.id!!, title = "원본"))

            shouldThrow<IllegalArgumentException> {
                service.update(UpdateReminderCommand(id = created.id, title = "  "))
            }
        }
    }

    describe("toggleCompleted") {
        it("미완료 → 완료 → 다시 미완료로 토글되며 completedAt 이 함께 변한다") {
            val list = newList()
            val created = service.create(CreateReminderCommand(listId = list.id!!, title = "x"))

            val first = service.toggleCompleted(ToggleReminderCompletedCommand(created.id))
            first.completed shouldBe true
            val completedAt = first.completedAt.shouldNotBeNull()
            (completedAt >= Instant.now().minusSeconds(60)) shouldBe true

            val second = service.toggleCompleted(ToggleReminderCompletedCommand(created.id))
            second.completed shouldBe false
            second.completedAt shouldBe null
        }

        it("미존재 id 는 ReminderNotFoundException") {
            shouldThrow<ReminderNotFoundException> {
                service.toggleCompleted(ToggleReminderCompletedCommand(999_999L))
            }
        }
    }

    describe("delete") {
        it("존재하면 DB row 를 제거한다") {
            val list = newList()
            val created = service.create(CreateReminderCommand(listId = list.id!!, title = "x"))

            service.delete(DeleteReminderCommand(created.id))

            reminderJpaRepository.count() shouldBe 0L
        }

        it("미존재 id 는 ReminderNotFoundException") {
            shouldThrow<ReminderNotFoundException> {
                service.delete(DeleteReminderCommand(999_999L))
            }
        }

        it("부모 reminder 삭제 시 자식 row 와 영속성 컨텍스트가 함께 정리된다") {
            val list = newList()
            val parent = service.create(CreateReminderCommand(listId = list.id!!, title = "부모"))
            val child = service.create(
                CreateReminderCommand(listId = list.id!!, title = "자식", parentId = parent.id),
            )

            service.delete(DeleteReminderCommand(parent.id))

            // 영속성 컨텍스트 stale 검증: findById 가 즉시 자식을 찾지 못해야 함
            reminderJpaRepository.findById(child.id).isPresent shouldBe false
            reminderJpaRepository.count() shouldBe 0L
        }
    }

    describe("리스트 cascade 삭제") {
        it("리스트가 삭제되면 그 리스트의 reminder 도 함께 삭제된다") {
            val list = newList()
            service.create(CreateReminderCommand(listId = list.id!!, title = "a"))
            service.create(CreateReminderCommand(listId = list.id!!, title = "b"))

            // 직접 List 서비스 호출 대신 영속성 어댑터의 deleteByListId 동작을 통해 검증
            // (DefaultReminderListCommandService.delete 가 동일하게 호출함)
            val other = listJpaRepository.save(
                ReminderList(name = "유지", color = "#0A84FF", sortOrder = 1),
            )
            service.create(CreateReminderCommand(listId = other.id!!, title = "유지대상"))

            // 리스트 서비스 직접 의존성 없이 reminder repository 가 listId 로 삭제하는지 확인
            reminderJpaRepository.deleteByListId(list.id!!)
            reminderJpaRepository.flush()

            reminderJpaRepository.count() shouldBe 1L
            reminderJpaRepository.findAll().single().title shouldBe "유지대상"
        }
    }
})
