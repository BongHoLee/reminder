package com.bong.reminder.list

import com.bong.reminder.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "reminder_list")
class ReminderList(
    name: String,
    color: String,
    sortOrder: Int = 0,
) : BaseEntity() {
    init {
        validateName(name)
        validateColor(color)
    }

    companion object {
        private val HEX_COLOR = Regex("^#[0-9A-Fa-f]{6}$")

        private fun validateName(name: String) {
            require(name.isNotBlank()) { "리스트 이름은 비어 있을 수 없습니다." }
        }

        private fun validateColor(color: String) {
            require(HEX_COLOR.matches(color)) { "색상은 #HEX 형식이어야 합니다." }
        }
    }

    @Column(nullable = false, length = 100)
    final var name: String = name
        private set

    @Column(nullable = false, length = 7)
    final var color: String = color
        private set

    @Column(nullable = false)
    final var sortOrder: Int = sortOrder
        private set

    fun rename(newName: String) {
        validateName(newName)
        this.name = newName
    }

    fun recolor(newColor: String) {
        validateColor(newColor)
        this.color = newColor
    }
}
