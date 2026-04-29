package com.bong.reminder.list.adapter.`in`.web

import com.bong.reminder.list.adapter.`in`.web.dto.ReminderListCreateRequest
import com.bong.reminder.list.adapter.`in`.web.dto.ReminderListUpdateRequest
import com.bong.reminder.list.application.query.ReminderListView
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class ReminderListWebMapperTest : DescribeSpec({

    describe("ReminderListCreateRequest.toCommand") {
        it("필드를 그대로 CreateReminderListCommand 로 옮긴다") {
            val command = ReminderListCreateRequest(name = "여행", color = "#0A84FF", sortOrder = 2)
                .toCommand()

            command.name shouldBe "여행"
            command.color shouldBe "#0A84FF"
            command.sortOrder shouldBe 2
        }
    }

    describe("ReminderListUpdateRequest.toCommand") {
        it("id 와 nullable 필드를 결합해 UpdateReminderListCommand 로 만든다") {
            val command = ReminderListUpdateRequest(name = "장보기", color = null, sortOrder = 3)
                .toCommand(id = 7L)

            command.id shouldBe 7L
            command.name shouldBe "장보기"
            command.color shouldBe null
            command.sortOrder shouldBe 3
        }
    }

    describe("ReminderListView.toResponse") {
        it("모든 필드를 ReminderListResponse 로 옮긴다") {
            val now = Instant.parse("2026-04-29T00:00:00Z")
            val response = ReminderListView(
                id = 1L,
                name = "쇼핑",
                color = "#FF9500",
                sortOrder = 0,
                createdAt = now,
                updatedAt = now,
            ).toResponse()

            response.id shouldBe 1L
            response.name shouldBe "쇼핑"
            response.color shouldBe "#FF9500"
            response.sortOrder shouldBe 0
            response.createdAt shouldBe now
            response.updatedAt shouldBe now
        }
    }
})
