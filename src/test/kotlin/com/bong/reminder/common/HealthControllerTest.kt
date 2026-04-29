package com.bong.reminder.common

import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
) : DescribeSpec({

    describe("GET /api/v1/health") {
        it("UP 을 반환한다") {
            mockMvc.get("/api/v1/health")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.status") { value("UP") }
                }
        }

        it("CORS preflight (OPTIONS) 가 http://localhost:3000 에 대해 허용된다") {
            mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .options("/api/v1/health")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "GET"),
            ).andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                    .header().string("Access-Control-Allow-Origin", "http://localhost:3000"),
            )
        }
    }
})
