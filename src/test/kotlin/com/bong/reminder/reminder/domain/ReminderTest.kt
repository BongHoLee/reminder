package com.bong.reminder.reminder.domain

import com.bong.reminder.list.domain.ReminderList
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class ReminderTest : DescribeSpec({

    val list = ReminderList(name = "쇼핑", color = "#FF9500")

    describe("생성") {
        it("정상 입력으로 생성된다 — 미완료 상태가 기본") {
            val r = Reminder(list = list, title = "우유 사기")

            r.title shouldBe "우유 사기"
            r.priority shouldBe Priority.NONE
            r.completed shouldBe false
            r.completedAt shouldBe null
            r.flagged shouldBe false
            r.sortOrder shouldBe 0
        }

        it("제목이 공백이면 거부한다") {
            val ex = shouldThrow<IllegalArgumentException> {
                Reminder(list = list, title = "  ")
            }
            ex.message shouldBe "미리 알림 제목은 비어 있을 수 없습니다."
        }

        it("제목이 500자를 넘으면 거부한다") {
            shouldThrow<IllegalArgumentException> {
                Reminder(list = list, title = "x".repeat(501))
            }
        }

        it("정렬 순서가 음수면 거부한다") {
            shouldThrow<IllegalArgumentException> {
                Reminder(list = list, title = "x", sortOrder = -1)
            }
        }
    }

    describe("toggleCompleted") {
        it("미완료 → 완료 시 completedAt 이 채워진다") {
            val r = Reminder(list = list, title = "x")
            val now = Instant.parse("2026-04-29T10:00:00Z")

            r.toggleCompleted(now)

            r.completed shouldBe true
            r.completedAt shouldBe now
        }

        it("완료 → 미완료 시 completedAt 이 비워진다") {
            val r = Reminder(list = list, title = "x")
            r.toggleCompleted(Instant.parse("2026-04-29T10:00:00Z"))

            r.toggleCompleted(Instant.parse("2026-04-29T11:00:00Z"))

            r.completed shouldBe false
            r.completedAt shouldBe null
        }
    }

    describe("changeParent") {
        it("부모의 부모가 있으면 거부한다 — 1단계 깊이 제한") {
            val grand = Reminder(list = list, title = "할아버지")
            val parent = Reminder(list = list, title = "아빠", parent = grand)
            val child = Reminder(list = list, title = "아들")

            shouldThrow<IllegalArgumentException> {
                child.changeParent(parent)
            }
        }

        it("자기 자신을 상위 작업으로 지정하면 id 가 null 이어도 거부한다") {
            val r = Reminder(list = list, title = "혼자")

            shouldThrow<IllegalArgumentException> {
                r.changeParent(r)
            }
        }

        it("같은 unpersisted 엔티티 둘은 서로 다른 reference 이면 parent 로 허용된다") {
            val parent = Reminder(list = list, title = "부모")
            val child = Reminder(list = list, title = "자식")

            child.changeParent(parent)

            child.parent shouldBe parent
        }
    }

    describe("도메인 메서드") {
        it("rename / changeNotes / changeDueAt / changePriority / changeFlagged / reorder") {
            val r = Reminder(list = list, title = "초안")

            r.rename("최종")
            r.changeNotes("메모".repeat(10))
            r.changeDueAt(Instant.parse("2026-05-01T09:00:00Z"))
            r.changePriority(Priority.HIGH)
            r.changeFlagged(true)
            r.reorder(7)

            r.title shouldBe "최종"
            r.notes shouldBe "메모".repeat(10)
            r.dueAt shouldBe Instant.parse("2026-05-01T09:00:00Z")
            r.priority shouldBe Priority.HIGH
            r.flagged shouldBe true
            r.sortOrder shouldBe 7
        }

        it("changeNotes 가 10000자를 넘으면 거부한다") {
            val r = Reminder(list = list, title = "x")
            shouldThrow<IllegalArgumentException> {
                r.changeNotes("a".repeat(10_001))
            }
        }
    }

    describe("assertCompletionConsistent") {
        it("completed=false 인데 completedAt 이 채워진 손상 상태이면 IllegalStateException 을 던진다") {
            val r = Reminder(list = list, title = "x")

            val completedAtField = Reminder::class.java.getDeclaredField("completedAt")
            completedAtField.isAccessible = true
            completedAtField.set(r, Instant.parse("2026-04-29T10:00:00Z"))

            r.completed shouldBe false
            r.completedAt shouldBe Instant.parse("2026-04-29T10:00:00Z")

            shouldThrow<IllegalStateException> {
                r.assertCompletionConsistent()
            }
        }
    }

    describe("notes 길이 검증") {
        it("생성자에 10000자 초과 notes 가 주어지면 IllegalArgumentException 을 던진다") {
            shouldThrow<IllegalArgumentException> {
                Reminder(list = list, title = "x", notes = "a".repeat(10_001))
            }
        }
    }
})
