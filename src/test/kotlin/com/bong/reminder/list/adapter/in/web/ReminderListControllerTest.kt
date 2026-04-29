package com.bong.reminder.list.adapter.`in`.web

import com.bong.reminder.list.adapter.out.persistence.ReminderListJpaRepository
import com.bong.reminder.list.domain.ReminderList
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReminderListControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val jpaRepository: ReminderListJpaRepository,
) : DescribeSpec({

    describe("GET /api/v1/lists") {
        it("저장된 리스트들을 sortOrder 오름차순으로 200 으로 반환한다") {
            jpaRepository.save(ReminderList(name = "업무", color = "#0A84FF", sortOrder = 1))
            jpaRepository.save(ReminderList(name = "쇼핑", color = "#FF9500", sortOrder = 0))

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
        it("정상 입력은 201 + Location + 본문 + DB 저장") {
            mockMvc.post("/api/v1/lists") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"여행","color":"#0A84FF","sortOrder":2}"""
            }.andExpect {
                status { isCreated() }
                header { exists("Location") }
                jsonPath("$.name") { value("여행") }
                jsonPath("$.color") { value("#0A84FF") }
                jsonPath("$.sortOrder") { value(2) }
            }

            jpaRepository.count() shouldBe 1L
            val saved = jpaRepository.findAll().single()
            saved.name shouldBe "여행"
            saved.color shouldBe "#0A84FF"
            saved.sortOrder shouldBe 2
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

            jpaRepository.count() shouldBe 0L
        }

        it("색상 길이가 7이 아니면 400") {
            mockMvc.post("/api/v1/lists") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"여행","color":"orange"}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("VALIDATION_ERROR") }
            }

            jpaRepository.count() shouldBe 0L
        }
    }

    describe("PATCH /api/v1/lists/{id}") {
        it("부분 수정 정상 200 + DB 반영") {
            val saved = jpaRepository.save(
                ReminderList(name = "쇼핑", color = "#FF9500", sortOrder = 0),
            )

            mockMvc.patch("/api/v1/lists/${saved.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"장보기","sortOrder":3}"""
            }.andExpect {
                status { isOk() }
                jsonPath("$.id") { value(saved.id!!.toInt()) }
                jsonPath("$.name") { value("장보기") }
                jsonPath("$.color") { value("#FF9500") }
                jsonPath("$.sortOrder") { value(3) }
            }

            val refetched = jpaRepository.findById(saved.id!!).orElseThrow()
            refetched.name shouldBe "장보기"
            refetched.color shouldBe "#FF9500"
            refetched.sortOrder shouldBe 3
        }

        it("미존재 id 는 404 + REMINDER_LIST_NOT_FOUND") {
            mockMvc.patch("/api/v1/lists/999999") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"x"}"""
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.code") { value("REMINDER_LIST_NOT_FOUND") }
                jsonPath("$.message") { value("리스트를 찾을 수 없습니다.") }
            }
        }

        it("도메인 검증 실패는 400 + INVALID_ARGUMENT") {
            val saved = jpaRepository.save(
                ReminderList(name = "쇼핑", color = "#FF9500", sortOrder = 0),
            )

            mockMvc.patch("/api/v1/lists/${saved.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name":"   "}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("INVALID_ARGUMENT") }
                jsonPath("$.message") { value("리스트 이름은 비어 있을 수 없습니다.") }
            }
        }
    }

    describe("DELETE /api/v1/lists/{id}") {
        it("정상 204 + DB row 제거") {
            val saved = jpaRepository.save(
                ReminderList(name = "쇼핑", color = "#FF9500", sortOrder = 0),
            )

            mockMvc.delete("/api/v1/lists/${saved.id}")
                .andExpect { status { isNoContent() } }

            jpaRepository.count() shouldBe 0L
        }

        it("미존재 id 는 404") {
            mockMvc.delete("/api/v1/lists/999999")
                .andExpect {
                    status { isNotFound() }
                    jsonPath("$.code") { value("REMINDER_LIST_NOT_FOUND") }
                }
        }
    }
})
