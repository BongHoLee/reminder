package com.bong.reminder.reminder.adapter.`in`.web

import com.bong.reminder.list.adapter.out.persistence.ReminderListJpaRepository
import com.bong.reminder.list.domain.ReminderList
import com.bong.reminder.reminder.adapter.out.persistence.ReminderJpaRepository
import com.bong.reminder.reminder.domain.Reminder
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(ReminderViewControllerTest.FixedClockConfig::class)
class ReminderViewControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val listJpaRepository: ReminderListJpaRepository,
    private val reminderJpaRepository: ReminderJpaRepository,
) : DescribeSpec({

    // 2026-04-29 (수) 정오 — Asia/Seoul 기준
    // KST 자정: 2026-04-28T15:00:00Z (전날) ~ 2026-04-29T15:00:00Z
    val seoul = ZoneId.of("Asia/Seoul")

    fun newList(): ReminderList = listJpaRepository.save(
        ReminderList(name = "L", color = "#000000"),
    )

    describe("GET /api/v1/views/today — 잘못된 tz") {
        it("유효하지 않은 timezone 은 400 + INVALID_TIMEZONE") {
            mockMvc.get("/api/v1/views/today?tz=Invalid/Zone")
                .andExpect {
                    status { isBadRequest() }
                    jsonPath("$.code") { value("INVALID_TIMEZONE") }
                }
        }
    }

    describe("GET /api/v1/views/today?tz=Asia/Seoul") {
        it("KST 자정 직후(00:00:01)는 오늘로, 어제 23:59:59와 내일 00:00:00 은 제외한다") {
            val list = newList()

            // 어제 23:59:59 KST → 오늘이 아님
            reminderJpaRepository.save(
                Reminder(
                    list = list,
                    title = "어제 자정 직전",
                    dueAt = Instant.parse("2026-04-28T14:59:59Z"),
                ),
            )
            // 오늘 00:00:00 KST 정확히
            reminderJpaRepository.save(
                Reminder(
                    list = list,
                    title = "오늘 자정",
                    dueAt = Instant.parse("2026-04-28T15:00:00Z"),
                ),
            )
            // 오늘 23:59:59 KST
            reminderJpaRepository.save(
                Reminder(
                    list = list,
                    title = "오늘 자정 직전",
                    dueAt = Instant.parse("2026-04-29T14:59:59Z"),
                ),
            )
            // 내일 00:00:00 KST → 오늘이 아님
            reminderJpaRepository.save(
                Reminder(
                    list = list,
                    title = "내일 자정",
                    dueAt = Instant.parse("2026-04-29T15:00:00Z"),
                ),
            )

            mockMvc.get("/api/v1/views/today?tz=Asia/Seoul")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].title") { value("오늘 자정") }
                    jsonPath("$[1].title") { value("오늘 자정 직전") }
                }
        }
    }

    describe("GET /api/v1/views/scheduled") {
        it("내일(KST 자정) 이후 미완료만 dueAt 오름차순으로 반환한다") {
            val list = newList()
            reminderJpaRepository.save(
                Reminder(list = list, title = "오늘", dueAt = Instant.parse("2026-04-29T03:00:00Z")),
            )
            reminderJpaRepository.save(
                Reminder(list = list, title = "내일", dueAt = Instant.parse("2026-04-29T15:00:00Z")),
            )
            reminderJpaRepository.save(
                Reminder(list = list, title = "모레", dueAt = Instant.parse("2026-05-01T03:00:00Z")),
            )

            mockMvc.get("/api/v1/views/scheduled?tz=Asia/Seoul")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(2) }
                    jsonPath("$[0].title") { value("내일") }
                    jsonPath("$[1].title") { value("모레") }
                }
        }
    }

    describe("GET /api/v1/views/all/flagged/completed") {
        it("미완료 / 깃발 / 완료 가 각각 분리되어 반환된다") {
            val list = newList()
            reminderJpaRepository.save(Reminder(list = list, title = "미완료-깃발", flagged = true))
            val done = reminderJpaRepository.save(Reminder(list = list, title = "완료"))
            done.toggleCompleted(Instant.now())
            reminderJpaRepository.flush()

            mockMvc.get("/api/v1/views/all").andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(1) }
                jsonPath("$[0].title") { value("미완료-깃발") }
            }
            mockMvc.get("/api/v1/views/flagged").andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(1) }
            }
            mockMvc.get("/api/v1/views/completed").andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(1) }
                jsonPath("$[0].title") { value("완료") }
            }
        }
    }

    describe("GET /api/v1/views/counts") {
        it("각 뷰의 카운트를 한 번에 돌려준다") {
            val list = newList()
            reminderJpaRepository.save(
                Reminder(list = list, title = "오늘", dueAt = Instant.parse("2026-04-29T03:00:00Z")),
            )
            reminderJpaRepository.save(
                Reminder(list = list, title = "예정", dueAt = Instant.parse("2026-05-01T03:00:00Z")),
            )
            reminderJpaRepository.save(Reminder(list = list, title = "깃발", flagged = true))
            val done = reminderJpaRepository.save(Reminder(list = list, title = "완료"))
            done.toggleCompleted(Instant.now())
            reminderJpaRepository.flush()

            mockMvc.get("/api/v1/views/counts?tz=Asia/Seoul")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.today") { value(1) }
                    jsonPath("$.scheduled") { value(1) }
                    jsonPath("$.all") { value(3) }
                    jsonPath("$.flagged") { value(1) }
                    jsonPath("$.completed") { value(1) }
                }
        }
    }

    describe("GET /api/v1/views/today — limit") {
        it("limit=2 일 때 최대 2건만 반환한다") {
            val list = newList()
            // FixedClock 기준 today (KST 2026-04-29) 에 해당하는 UTC 4건 저장.
            listOf("a", "b", "c", "d").forEachIndexed { idx, t ->
                reminderJpaRepository.save(
                    Reminder(
                        list = list,
                        title = t,
                        dueAt = Instant.parse("2026-04-29T0${idx}:00:00Z"),
                    ),
                )
            }
            mockMvc.get("/api/v1/views/today?tz=Asia/Seoul&limit=2")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()") { value(2) }
                }
        }
    }
}) {
    @TestConfiguration
    class FixedClockConfig {
        @Bean
        @Primary
        fun fixedClock(): Clock = Clock.fixed(
            Instant.parse("2026-04-29T03:00:00Z"), // KST 12:00 정오
            ZoneId.of("UTC"),
        )
    }
}
