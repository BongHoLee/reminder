package com.bong.reminder.reminder.adapter.`in`.web

import com.bong.reminder.list.adapter.out.persistence.ReminderListJpaRepository
import com.bong.reminder.list.domain.ReminderList
import com.bong.reminder.reminder.adapter.out.persistence.ReminderJpaRepository
import com.bong.reminder.reminder.domain.Reminder
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReminderSearchControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val listJpaRepository: ReminderListJpaRepository,
    private val reminderJpaRepository: ReminderJpaRepository,
) : DescribeSpec({

    describe("GET /api/v1/search") {
        it("title 부분 일치, 대소문자 무시") {
            val list = listJpaRepository.save(ReminderList(name = "쇼핑", color = "#FF9500"))
            reminderJpaRepository.save(Reminder(list = list, title = "Buy Milk"))
            reminderJpaRepository.save(Reminder(list = list, title = "친구 만나기"))

            mockMvc.get("/api/v1/search?q=milk")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(1) }
                    jsonPath("$[0].title") { value("Buy Milk") }
                }
        }

        it("notes 부분 일치도 검색된다") {
            val list = listJpaRepository.save(ReminderList(name = "업무", color = "#0A84FF"))
            reminderJpaRepository.save(
                Reminder(list = list, title = "회의", notes = "분기 KPI 검토"),
            )
            reminderJpaRepository.save(Reminder(list = list, title = "점심"))

            mockMvc.get("/api/v1/search?q=KPI")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(1) }
                    jsonPath("$[0].title") { value("회의") }
                }
        }

        it("빈 q 는 빈 배열") {
            mockMvc.get("/api/v1/search?q=")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(0) }
                }
        }

        it("q 파라미터 누락 시 400") {
            mockMvc.get("/api/v1/search")
                .andExpect { status { isBadRequest() } }
        }

        it("q 가 200자를 초과하면 400") {
            mockMvc.get("/api/v1/search?q=${"x".repeat(201)}")
                .andExpect { status { isBadRequest() } }
        }

        it("동일 updatedAt 일 때 id 내림차순으로 결정적 정렬된다") {
            val list = listJpaRepository.save(ReminderList(name = "x", color = "#000000"))
            val a = reminderJpaRepository.save(Reminder(list = list, title = "match a"))
            val b = reminderJpaRepository.save(Reminder(list = list, title = "match b"))
            val c = reminderJpaRepository.save(Reminder(list = list, title = "match c"))

            mockMvc.get("/api/v1/search?q=match")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(3) }
                    jsonPath("$[0].id") { value(c.id!!) }
                    jsonPath("$[1].id") { value(b.id!!) }
                    jsonPath("$[2].id") { value(a.id!!) }
                }
        }
    }
})
