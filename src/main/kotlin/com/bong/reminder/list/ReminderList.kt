package com.bong.reminder.list

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "reminder_list")
@EntityListeners(AuditingEntityListener::class)
class ReminderList(
    name: String,
    color: String,
    sortOrder: Int = 0,
) {
    init {
        validateName(name)
        validateColor(color)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    final var id: Long? = null
        private set

    @Column(nullable = false, length = 100)
    final var name: String = name
        private set

    @Column(nullable = false, length = 7)
    final var color: String = color
        private set

    @Column(nullable = false)
    final var sortOrder: Int = sortOrder
        private set

    @CreatedDate
    @Column(nullable = false, updatable = false)
    final var createdAt: Instant = Instant.EPOCH
        private set

    @LastModifiedDate
    @Column(nullable = false)
    final var updatedAt: Instant = Instant.EPOCH
        private set

    fun rename(newName: String) {
        validateName(newName)
        this.name = newName
    }

    fun recolor(newColor: String) {
        validateColor(newColor)
        this.color = newColor
    }

    companion object {
        private val HEX_COLOR = Regex("^#[0-9A-Fa-f]{6}$")

        private fun validateName(name: String) {
            require(name.isNotBlank()) { "리스트 이름은 비어 있을 수 없습니다." }
        }

        private fun validateColor(color: String) {
            require(HEX_COLOR.matches(color)) { "색상은 #RRGGBB 형식이어야 합니다." }
        }
    }
}
