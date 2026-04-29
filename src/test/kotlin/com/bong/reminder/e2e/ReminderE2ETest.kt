package com.bong.reminder.e2e

import com.bong.reminder.common.ErrorResponse
import com.bong.reminder.list.adapter.`in`.web.dto.ReminderListResponse
import com.bong.reminder.list.adapter.out.persistence.ReminderListJpaRepository
import com.bong.reminder.reminder.adapter.`in`.web.dto.ReminderResponse
import com.bong.reminder.reminder.adapter.out.persistence.ReminderJpaRepository
import tools.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReminderE2ETest @Autowired constructor(
    private val listJpaRepository: ReminderListJpaRepository,
    private val reminderJpaRepository: ReminderJpaRepository,
    private val objectMapper: ObjectMapper,
    @LocalServerPort private val port: Int,
) : DescribeSpec({

    val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()
    val baseUrl = "http://localhost:$port/api/v1"

    fun call(method: String, path: String, body: String? = null): HttpResponse<String> {
        val publisher = if (body != null) {
            HttpRequest.BodyPublishers.ofString(body)
        } else {
            HttpRequest.BodyPublishers.noBody()
        }
        val builder = HttpRequest.newBuilder(URI.create("$baseUrl$path"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(10))
            .method(method, publisher)
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    fun <T> parse(body: String, clazz: Class<T>): T = objectMapper.readValue(body, clazz)

    fun parseListResponse(body: String): ReminderListResponse =
        parse(body, ReminderListResponse::class.java)

    fun parseReminderResponse(body: String): ReminderResponse =
        parse(body, ReminderResponse::class.java)

    fun parseListArray(body: String): Array<ReminderListResponse> =
        parse(body, Array<ReminderListResponse>::class.java)

    fun parseReminderArray(body: String): Array<ReminderResponse> =
        parse(body, Array<ReminderResponse>::class.java)

    fun parseError(body: String): ErrorResponse =
        parse(body, ErrorResponse::class.java)

    beforeEach {
        // 테스트 간 격리 — 각 시나리오는 비어 있는 DB 에서 시작
        reminderJpaRepository.deleteAllInBatch()
        listJpaRepository.deleteAllInBatch()
    }

    describe("리스트 CRUD 시나리오") {
        it("생성 → 목록 조회 → 부분 수정 → 삭제 흐름이 일관된다") {
            val createResp = call("POST", "/lists", """{"name":"쇼핑","color":"#FF9500","sortOrder":0}""")
            createResp.statusCode() shouldBe 201
            createResp.headers().firstValue("Location").isPresent shouldBe true
            val created = parseListResponse(createResp.body())
            created.name shouldBe "쇼핑"
            created.color shouldBe "#FF9500"

            val getResp = call("GET", "/lists")
            getResp.statusCode() shouldBe 200
            parseListArray(getResp.body()) shouldHaveSize 1

            val patchResp = call("PATCH", "/lists/${created.id}", """{"name":"장보기","sortOrder":2}""")
            patchResp.statusCode() shouldBe 200
            val updated = parseListResponse(patchResp.body())
            updated.name shouldBe "장보기"
            updated.color shouldBe "#FF9500"
            updated.sortOrder shouldBe 2

            val deleteResp = call("DELETE", "/lists/${created.id}")
            deleteResp.statusCode() shouldBe 204

            parseListArray(call("GET", "/lists").body()) shouldHaveSize 0
        }
    }

    describe("Reminder 풀 라이프사이클") {
        it("리스트 생성 → reminder 생성 → 부분 수정 → toggle → 완료 조회 → 토글 복원 → 삭제") {
            val list = parseListResponse(
                call("POST", "/lists", """{"name":"업무","color":"#0A84FF","sortOrder":0}""").body(),
            )

            val createResp = call(
                "POST",
                "/lists/${list.id}/reminders",
                """{"title":"기획서 작성","priority":"HIGH","sortOrder":0}""",
            )
            createResp.statusCode() shouldBe 201
            val reminder = parseReminderResponse(createResp.body())
            reminder.title shouldBe "기획서 작성"
            reminder.completed shouldBe false
            reminder.listId shouldBe list.id

            parseReminderArray(call("GET", "/lists/${list.id}/reminders").body()) shouldHaveSize 1

            val patchResp = call(
                "PATCH",
                "/reminders/${reminder.id}",
                """{"title":"기획서 v2","flagged":true}""",
            )
            patchResp.statusCode() shouldBe 200
            val patched = parseReminderResponse(patchResp.body())
            patched.title shouldBe "기획서 v2"
            patched.flagged shouldBe true

            val toggleResp = call("POST", "/reminders/${reminder.id}/toggle")
            toggleResp.statusCode() shouldBe 200
            val toggled = parseReminderResponse(toggleResp.body())
            toggled.completed shouldBe true
            toggled.completedAt.shouldNotBeNull()

            // 미완료 기본 조회 → 0건 / 완료 조회 → 1건
            parseReminderArray(
                call("GET", "/lists/${list.id}/reminders").body(),
            ) shouldHaveSize 0
            parseReminderArray(
                call("GET", "/lists/${list.id}/reminders?completed=true").body(),
            ) shouldHaveSize 1

            val toggleBack = parseReminderResponse(
                call("POST", "/reminders/${reminder.id}/toggle").body(),
            )
            toggleBack.completed shouldBe false
            toggleBack.completedAt shouldBe null

            call("DELETE", "/reminders/${reminder.id}").statusCode() shouldBe 204
            parseReminderArray(
                call("GET", "/lists/${list.id}/reminders").body(),
            ) shouldHaveSize 0
        }
    }

    describe("리스트 cascade 삭제") {
        it("DELETE /lists/{id} 시 그 리스트의 reminder 들도 함께 사라진다") {
            val list = parseListResponse(
                call("POST", "/lists", """{"name":"여행","color":"#34C759","sortOrder":0}""").body(),
            )
            call("POST", "/lists/${list.id}/reminders", """{"title":"여권"}""").statusCode() shouldBe 201
            call("POST", "/lists/${list.id}/reminders", """{"title":"항공권"}""").statusCode() shouldBe 201

            call("DELETE", "/lists/${list.id}").statusCode() shouldBe 204

            // 미존재 listId 로 reminders 조회 → 404
            val followUp = call("GET", "/lists/${list.id}/reminders")
            followUp.statusCode() shouldBe 404
            parseError(followUp.body()).code shouldBe "REMINDER_LIST_NOT_FOUND"

            reminderJpaRepository.count() shouldBe 0L
            listJpaRepository.count() shouldBe 0L
        }
    }

    describe("Validation 및 에러 매핑") {
        it("POST /lists 이름 공백 → 400 + VALIDATION_ERROR + fieldErrors") {
            val resp = call("POST", "/lists", """{"name":"   ","color":"#FF9500"}""")
            resp.statusCode() shouldBe 400
            val err = parseError(resp.body())
            err.code shouldBe "VALIDATION_ERROR"
            err.fieldErrors.any { it.field == "name" } shouldBe true
        }

        it("미존재 reminder 토글 → 404 + REMINDER_NOT_FOUND") {
            val resp = call("POST", "/reminders/999999/toggle")
            resp.statusCode() shouldBe 404
            parseError(resp.body()).code shouldBe "REMINDER_NOT_FOUND"
        }

        it("도메인 검증 실패(공백 title PATCH)는 400 + INVALID_ARGUMENT") {
            val list = parseListResponse(
                call("POST", "/lists", """{"name":"x","color":"#FF9500","sortOrder":0}""").body(),
            )
            val r = parseReminderResponse(
                call("POST", "/lists/${list.id}/reminders", """{"title":"원본"}""").body(),
            )

            val resp = call("PATCH", "/reminders/${r.id}", """{"title":"   "}""")
            resp.statusCode() shouldBe 400
            parseError(resp.body()).code shouldBe "INVALID_ARGUMENT"
        }
    }
})
