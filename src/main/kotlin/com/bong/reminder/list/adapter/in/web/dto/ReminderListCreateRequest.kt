package com.bong.reminder.list.adapter.`in`.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ReminderListCreateRequest(
    @field:NotBlank(message = "리스트 이름은 비어 있을 수 없습니다.")
    @field:Size(max = 100, message = "리스트 이름은 100자 이하여야 합니다.")
    val name: String,

    @field:NotBlank(message = "색상은 비어 있을 수 없습니다.")
    @field:Size(min = 7, max = 7, message = "색상은 #RRGGBB 형식이어야 합니다.")
    val color: String,

    val sortOrder: Int = 0,
)
