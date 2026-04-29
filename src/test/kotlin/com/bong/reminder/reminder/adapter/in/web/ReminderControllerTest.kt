package com.bong.reminder.reminder.adapter.`in`.web

import com.bong.reminder.list.adapter.out.persistence.ReminderListJpaRepository
import com.bong.reminder.list.domain.ReminderList
import com.bong.reminder.reminder.adapter.out.persistence.ReminderJpaRepository
import com.bong.reminder.reminder.domain.Priority
import com.bong.reminder.reminder.domain.Reminder
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
class ReminderControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val listJpaRepository: ReminderListJpaRepository,
    private val reminderJpaRepository: ReminderJpaRepository,
) : DescribeSpec({

    fun newList() = listJpaRepository.save(
        ReminderList(name = "쇼핑", color = "#FF9500", sortOrder = 0),
    )

    describe("GET /api/v1/lists/{listId}/reminders") {
        it("미완료 reminder 를 sortOrder 오름차순으로 반환한다") {
            val list = newList()
            reminderJpaRepository.save(Reminder(list = list, title = "B", sortOrder = 1))
            reminderJpaRepository.save(Reminder(list = list, title = "A", sortOrder = 0))

            mockMvc.get("/api/v1/lists/${list.id}/reminders")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].title") { value("A") }
                    jsonPath("$[1].title") { value("B") }
                }
        }

        it("미존재 listId 는 404 + REMINDER_LIST_NOT_FOUND") {
            mockMvc.get("/api/v1/lists/999999/reminders")
                .andExpect {
                    status { isNotFound() }
                    jsonPath("$.code") { value("REMINDER_LIST_NOT_FOUND") }
                }
        }

        it("completed=true 쿼리는 완료된 reminder 만 반환한다") {
            val list = newList()
            val done = reminderJpaRepository.save(Reminder(list = list, title = "완료된 것"))
            done.toggleCompleted(java.time.Instant.now())
            reminderJpaRepository.save(Reminder(list = list, title = "미완료"))
            reminderJpaRepository.flush()

            mockMvc.get("/api/v1/lists/${list.id}/reminders?completed=true")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(1) }
                    jsonPath("$[0].title") { value("완료된 것") }
                }
        }
    }

    describe("POST /api/v1/lists/{listId}/reminders") {
        it("정상 입력은 201 + Location + DB 저장") {
            val list = newList()

            mockMvc.post("/api/v1/lists/${list.id}/reminders") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"title":"우유 사기","priority":"HIGH","flagged":true,"sortOrder":2}"""
            }.andExpect {
                status { isCreated() }
                header { exists("Location") }
                jsonPath("$.title") { value("우유 사기") }
                jsonPath("$.priority") { value("HIGH") }
                jsonPath("$.flagged") { value(true) }
                jsonPath("$.sortOrder") { value(2) }
                jsonPath("$.completed") { value(false) }
                jsonPath("$.listId") { value(list.id!!.toInt()) }
            }

            reminderJpaRepository.count() shouldBe 1L
        }

        it("title 공백은 400 + VALIDATION_ERROR") {
            val list = newList()

            mockMvc.post("/api/v1/lists/${list.id}/reminders") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"title":"   "}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("VALIDATION_ERROR") }
                jsonPath("$.fieldErrors[?(@.field=='title')].message") { exists() }
            }

            reminderJpaRepository.count() shouldBe 0L
        }

        it("미존재 listId 는 404 + REMINDER_LIST_NOT_FOUND") {
            mockMvc.post("/api/v1/lists/999999/reminders") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"title":"x"}"""
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.code") { value("REMINDER_LIST_NOT_FOUND") }
            }
        }

        it("이미 부모인 reminder 의 자식으로 등록 시도 → 400 INVALID_ARGUMENT (1단계 깊이 제한)") {
            val list = newList()
            val grand = reminderJpaRepository.save(Reminder(list = list, title = "조부"))
            val parent = reminderJpaRepository.save(Reminder(list = list, title = "부", parent = grand))

            mockMvc.post("/api/v1/lists/${list.id}/reminders") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"title":"손주","parentId":${parent.id}}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("INVALID_ARGUMENT") }
            }
        }
    }

    describe("PATCH /api/v1/reminders/{id}") {
        it("부분 수정 정상 200 + DB 반영") {
            val list = newList()
            val saved = reminderJpaRepository.save(
                Reminder(list = list, title = "초안", priority = Priority.NONE, sortOrder = 0),
            )

            mockMvc.patch("/api/v1/reminders/${saved.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"title":"최종","priority":"MEDIUM","flagged":true}"""
            }.andExpect {
                status { isOk() }
                jsonPath("$.title") { value("최종") }
                jsonPath("$.priority") { value("MEDIUM") }
                jsonPath("$.flagged") { value(true) }
            }

            val refetched = reminderJpaRepository.findById(saved.id!!).orElseThrow()
            refetched.title shouldBe "최종"
            refetched.priority shouldBe Priority.MEDIUM
            refetched.flagged shouldBe true
        }

        it("미존재 id 는 404 + REMINDER_NOT_FOUND") {
            mockMvc.patch("/api/v1/reminders/999999") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"title":"x"}"""
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.code") { value("REMINDER_NOT_FOUND") }
            }
        }

        it("도메인 검증 실패는 400 + INVALID_ARGUMENT") {
            val list = newList()
            val saved = reminderJpaRepository.save(Reminder(list = list, title = "원본"))

            mockMvc.patch("/api/v1/reminders/${saved.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"title":"   "}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("INVALID_ARGUMENT") }
            }
        }

        it("dueAtClear=true 는 dueAt 을 null 로 비운다 (명시적 clear)") {
            val list = newList()
            val saved = reminderJpaRepository.save(
                Reminder(
                    list = list,
                    title = "x",
                    dueAt = java.time.Instant.parse("2026-04-30T08:00:00Z"),
                ),
            )

            mockMvc.patch("/api/v1/reminders/${saved.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"dueAtClear":true}"""
            }.andExpect {
                status { isOk() }
                jsonPath("$.dueAt") { value(null) }
            }

            reminderJpaRepository.findById(saved.id!!).orElseThrow().dueAt shouldBe null
        }

        it("notesClear=true 는 notes 를 null 로 비운다") {
            val list = newList()
            val saved = reminderJpaRepository.save(
                Reminder(list = list, title = "x", notes = "기존 메모"),
            )

            mockMvc.patch("/api/v1/reminders/${saved.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"notesClear":true}"""
            }.andExpect {
                status { isOk() }
                jsonPath("$.notes") { value(null) }
            }
        }

        it("parentIdClear=true 는 parent 를 null 로 만든다 (Shift+Tab outdent)") {
            val list = newList()
            val parent = reminderJpaRepository.save(Reminder(list = list, title = "부모"))
            val child = reminderJpaRepository.save(
                Reminder(list = list, title = "자식", parent = parent),
            )

            mockMvc.patch("/api/v1/reminders/${child.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"parentIdClear":true}"""
            }.andExpect {
                status { isOk() }
                jsonPath("$.parentId") { value(null) }
            }

            reminderJpaRepository.findById(child.id!!).orElseThrow().parent shouldBe null
        }

        it("dueAtClear=true 와 dueAt=값 을 동시에 보내면 400 INVALID_ARGUMENT") {
            val list = newList()
            val saved = reminderJpaRepository.save(Reminder(list = list, title = "x"))

            mockMvc.patch("/api/v1/reminders/${saved.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"dueAt":"2026-05-01T00:00:00Z","dueAtClear":true}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("INVALID_ARGUMENT") }
            }
        }
    }

    describe("POST /api/v1/reminders/{id}/toggle") {
        it("미완료 → 완료 200 + DB 반영") {
            val list = newList()
            val saved = reminderJpaRepository.save(Reminder(list = list, title = "x"))

            mockMvc.post("/api/v1/reminders/${saved.id}/toggle")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.completed") { value(true) }
                    jsonPath("$.completedAt") { exists() }
                }

            val refetched = reminderJpaRepository.findById(saved.id!!).orElseThrow()
            refetched.completed shouldBe true
        }

        it("미존재 id 는 404") {
            mockMvc.post("/api/v1/reminders/999999/toggle")
                .andExpect {
                    status { isNotFound() }
                    jsonPath("$.code") { value("REMINDER_NOT_FOUND") }
                }
        }
    }

    describe("DELETE /api/v1/reminders/{id}") {
        it("정상 204 + DB row 제거") {
            val list = newList()
            val saved = reminderJpaRepository.save(Reminder(list = list, title = "x"))

            mockMvc.delete("/api/v1/reminders/${saved.id}")
                .andExpect { status { isNoContent() } }

            reminderJpaRepository.count() shouldBe 0L
        }

        it("미존재 id 는 404") {
            mockMvc.delete("/api/v1/reminders/999999")
                .andExpect {
                    status { isNotFound() }
                    jsonPath("$.code") { value("REMINDER_NOT_FOUND") }
                }
        }
    }

    describe("GET /api/v1/reminders/{id}/children") {
        it("부모 reminder 의 자식들을 sortOrder 오름차순으로 반환한다") {
            val list = newList()
            val parent = reminderJpaRepository.save(Reminder(list = list, title = "장보기"))
            reminderJpaRepository.save(Reminder(list = list, title = "우유", parent = parent, sortOrder = 1))
            reminderJpaRepository.save(Reminder(list = list, title = "빵", parent = parent, sortOrder = 0))

            mockMvc.get("/api/v1/reminders/${parent.id}/children")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].title") { value("빵") }
                    jsonPath("$[1].title") { value("우유") }
                }
        }

        it("미존재 parent id 는 404") {
            mockMvc.get("/api/v1/reminders/999999/children")
                .andExpect {
                    status { isNotFound() }
                    jsonPath("$.code") { value("REMINDER_NOT_FOUND") }
                }
        }
    }

    describe("리스트 cascade 삭제") {
        it("DELETE /api/v1/lists/{id} 시 그 리스트의 reminder 도 함께 삭제된다") {
            val list = newList()
            reminderJpaRepository.save(Reminder(list = list, title = "a"))
            reminderJpaRepository.save(Reminder(list = list, title = "b"))

            mockMvc.delete("/api/v1/lists/${list.id}")
                .andExpect { status { isNoContent() } }

            reminderJpaRepository.count() shouldBe 0L
            listJpaRepository.findById(list.id!!).isPresent shouldBe false
        }

        it("자기참조 부모-자식 reminder 가 섞여 있어도 cascade 가 성공한다") {
            val list = newList()
            val parent = reminderJpaRepository.save(Reminder(list = list, title = "장보기"))
            reminderJpaRepository.save(Reminder(list = list, title = "우유", parent = parent))
            reminderJpaRepository.save(Reminder(list = list, title = "빵", parent = parent))

            mockMvc.delete("/api/v1/lists/${list.id}")
                .andExpect { status { isNoContent() } }

            reminderJpaRepository.count() shouldBe 0L
        }
    }
})
