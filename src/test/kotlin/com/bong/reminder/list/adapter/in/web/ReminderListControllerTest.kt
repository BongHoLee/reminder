package com.bong.reminder.list.adapter.`in`.web

import com.bong.reminder.common.GlobalExceptionHandler
import com.bong.reminder.list.application.command.CreateReminderListCommand
import com.bong.reminder.list.application.command.DeleteReminderListCommand
import com.bong.reminder.list.application.command.UpdateReminderListCommand
import com.bong.reminder.list.application.port.`in`.ReminderListCommandService
import com.bong.reminder.list.application.port.`in`.ReminderListQueryService
import com.bong.reminder.list.application.query.ReminderListView
import com.bong.reminder.list.domain.ReminderListNotFoundException
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.verify
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(ReminderListController::class)
@Import(GlobalExceptionHandler::class)
class ReminderListControllerTest(
    private val mockMvc: MockMvc,
    @MockkBean private val commandService: ReminderListCommandService,
    @MockkBean private val queryService: ReminderListQueryService,
) : DescribeSpec({

    val now = Instant.parse("2026-04-29T10:00:00Z")

    fun view(
        id: Long = 1L,
        name: String = "쇼핑",
        color: String = "#FF9500",
        sortOrder: Int = 0,
    ) = ReminderListView(
        id = id,
        name = name,
        color = color,
        sortOrder = sortOrder,
        createdAt = now,
        updatedAt = now,
    )

    describe("GET /api/v1/lists") {
        it("리스트 배열을 200 으로 반환한다") {
            every { queryService.findAll() } returns listOf(
                view(id = 1L, name = "쇼핑", sortOrder = 0),
                view(id = 2L, name = "업무", sortOrder = 1),
            )

            mockMvc.get("/api/v1/lists")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].name") { value("쇼핑") }
                    jsonPath("$[1].name") { value("업무") }
                }
        }
    }

    describe("POST /api/v1/lists") {
        it("정상 입력은 201 + Location + 본문") {
            every { commandService.create(any()) } returns view(id = 10L, name = "여행", color = "#0A84FF", sortOrder = 2)

            mockMvc.post("/api/v1/lists") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"여행","color":"#0A84FF","sortOrder":2}"""
            }.andExpect {
                status { isCreated() }
                header { string("Location", "/api/v1/lists/10") }
                jsonPath("$.id") { value(10) }
                jsonPath("$.name") { value("여행") }
            }

            verify(exactly = 1) {
                commandService.create(
                    CreateReminderListCommand(name = "여행", color = "#0A84FF", sortOrder = 2),
                )
            }
        }

        it("이름 공백은 400 + VALIDATION_ERROR + fieldErrors") {
            mockMvc.post("/api/v1/lists") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"   ","color":"#0A84FF"}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("VALIDATION_ERROR") }
                jsonPath("$.fieldErrors[?(@.field=='name')].message") { exists() }
            }
        }

        it("색상 길이가 7이 아니면 400") {
            mockMvc.post("/api/v1/lists") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"여행","color":"orange"}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("VALIDATION_ERROR") }
            }
        }
    }

    describe("PATCH /api/v1/lists/{id}") {
        it("부분 수정 정상 200") {
            every { commandService.update(any()) } returns view(id = 5L, name = "장보기", color = "#FF9500", sortOrder = 3)

            mockMvc.patch("/api/v1/lists/5") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"장보기","sortOrder":3}"""
            }.andExpect {
                status { isOk() }
                jsonPath("$.id") { value(5) }
                jsonPath("$.name") { value("장보기") }
                jsonPath("$.sortOrder") { value(3) }
            }

            verify(exactly = 1) {
                commandService.update(
                    UpdateReminderListCommand(id = 5L, name = "장보기", color = null, sortOrder = 3),
                )
            }
        }

        it("미존재 id 는 404 + REMINDER_LIST_NOT_FOUND") {
            every { commandService.update(any()) } throws ReminderListNotFoundException()

            mockMvc.patch("/api/v1/lists/999") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"x"}"""
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.code") { value("REMINDER_LIST_NOT_FOUND") }
                jsonPath("$.message") { value("리스트를 찾을 수 없습니다.") }
            }
        }

        it("도메인 검증 실패는 400 + INVALID_ARGUMENT") {
            every { commandService.update(any()) } throws IllegalArgumentException("색상은 #HEX 형식이어야 합니다.")

            mockMvc.patch("/api/v1/lists/1") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"color":"AAAAAAA"}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("INVALID_ARGUMENT") }
                jsonPath("$.message") { value("색상은 #HEX 형식이어야 합니다.") }
            }
        }
    }

    describe("DELETE /api/v1/lists/{id}") {
        it("정상 204") {
            every { commandService.delete(DeleteReminderListCommand(7L)) } returns Unit

            mockMvc.delete("/api/v1/lists/7")
                .andExpect { status { isNoContent() } }

            verify(exactly = 1) { commandService.delete(DeleteReminderListCommand(7L)) }
        }

        it("미존재 id 는 404") {
            every { commandService.delete(any()) } throws ReminderListNotFoundException()

            mockMvc.delete("/api/v1/lists/999")
                .andExpect {
                    status { isNotFound() }
                    jsonPath("$.code") { value("REMINDER_LIST_NOT_FOUND") }
                }
        }
    }
})
