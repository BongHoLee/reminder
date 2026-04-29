package com.bong.reminder.list

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ReminderListTest : DescribeSpec({

    describe("생성") {
        it("정상 입력으로 생성된다") {
            val list = ReminderList(name = "쇼핑", color = "#FF9500", sortOrder = 1)

            list.name shouldBe "쇼핑"
            list.color shouldBe "#FF9500"
            list.sortOrder shouldBe 1
            list.id shouldBe null
        }

        it("이름이 공백이면 거부한다") {
            val ex = shouldThrow<IllegalArgumentException> {
                ReminderList(name = "  ", color = "#FF9500")
            }
            ex.message shouldBe "리스트 이름은 비어 있을 수 없습니다."
        }

        it("색상이 hex 형식이 아니면 거부한다") {
            val ex = shouldThrow<IllegalArgumentException> {
                ReminderList(name = "쇼핑", color = "orange")
            }
            ex.message shouldBe "색상은 #HEX 형식이어야 합니다."
        }

        it("색상이 짧은 hex(#RGB)면 거부한다") {
            shouldThrow<IllegalArgumentException> {
                ReminderList(name = "쇼핑", color = "#F90")
            }
        }
    }

    describe("rename") {
        it("이름을 변경한다") {
            val list = ReminderList(name = "쇼핑", color = "#FF9500")

            list.rename("장보기")

            list.name shouldBe "장보기"
        }

        it("공백 이름으로의 변경을 거부한다") {
            val list = ReminderList(name = "쇼핑", color = "#FF9500")

            shouldThrow<IllegalArgumentException> {
                list.rename("")
            }
            list.name shouldBe "쇼핑"
        }
    }

    describe("recolor") {
        it("색상을 변경한다") {
            val list = ReminderList(name = "쇼핑", color = "#FF9500")

            list.recolor("#0A84FF")

            list.color shouldBe "#0A84FF"
        }

        it("잘못된 형식의 색상으로의 변경을 거부한다") {
            val list = ReminderList(name = "쇼핑", color = "#FF9500")

            shouldThrow<IllegalArgumentException> {
                list.recolor("FF9500")
            }
            list.color shouldBe "#FF9500"
        }
    }
})
